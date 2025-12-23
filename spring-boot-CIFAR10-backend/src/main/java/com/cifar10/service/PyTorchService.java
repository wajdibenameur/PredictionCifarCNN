package com.cifar10.service;

import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.*;
import com.cifar10.config.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PyTorchService {

    private static final Logger logger = LoggerFactory.getLogger(PyTorchService.class);

    private final ModelConfig modelConfig;
    private final ResourceLoader resourceLoader;

    private ZooModel<Image, Classifications> model;
    private Predictor<Image, Classifications> predictor;

    public PyTorchService(ModelConfig modelConfig, ResourceLoader resourceLoader) {
        this.modelConfig = modelConfig;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        logger.info("🚀 Chargement du modèle PyTorch pour CIFAR-10...");
        logger.info("Configuration: inputSize={}, classes={}",
                modelConfig.getInputSize(), modelConfig.getClasses());

        try {
            Resource modelResource = resourceLoader.getResource("classpath:models/cifar10/traced_model_cpu_trained.pt");
            logger.info("Modèle trouvé: {}", modelResource.exists() ? "OUI" : "NON");

            Translator<Image, Classifications> translator = new Translator<>() {
                private Pipeline pipeline;

                @Override
                public void prepare(TranslatorContext ctx) {
                    float[] mean = new float[modelConfig.getMean().size()];
                    for (int i = 0; i < mean.length; i++) mean[i] = modelConfig.getMean().get(i);

                    float[] std = new float[modelConfig.getStd().size()];
                    for (int i = 0; i < std.length; i++) std[i] = modelConfig.getStd().get(i);

                    int inputSize = modelConfig.getInputSize();
                    logger.debug("Préparation du pipeline: inputSize={}, mean={}, std={}",
                            inputSize, mean, std);

                    pipeline = new Pipeline()
                            .add(new Resize(inputSize, inputSize))
                            .add(new ToTensor())
                            .add(new Normalize(mean, std));
                }

                @Override
                public NDList processInput(TranslatorContext ctx, Image input) {
                    try {
                        // Convertir l'image en NDArray [H, W, C]
                        NDArray array = input.toNDArray(ctx.getNDManager());
                        logger.debug("Image shape originale: {}", array.getShape());

                        // Appliquer le pipeline
                        NDList processed = pipeline.transform(new NDList(array));
                        NDArray tensor = processed.singletonOrThrow();
                        logger.debug("Tensor après pipeline: {}", tensor.getShape());

                        // Ajouter dimension batch: [C, H, W] -> [1, C, H, W]
                        if (tensor.getShape().dimension() == 3) {
                            tensor = tensor.expandDims(0);
                        }

                        logger.debug("Tensor final pour modèle: {}", tensor.getShape());
                        return new NDList(tensor);
                    } catch (Exception e) {
                        logger.error("Erreur dans processInput", e);
                        throw new RuntimeException("Prétraitement d'image échoué", e);
                    }
                }

                @Override
                public Classifications processOutput(TranslatorContext ctx, NDList list) {
                    try {
                        NDArray output = list.get(0);
                        logger.debug("Sortie du modèle: {}", output.getShape());

                        NDArray probs = output.softmax(1);

                        // Retirer la dimension batch si nécessaire
                        if (probs.getShape().dimension() == 2 && probs.getShape().get(0) == 1) {
                            probs = probs.squeeze(0);
                        }

                        logger.debug("Probabilités finales: {}", probs.getShape());
                        return new Classifications(modelConfig.getClasses(), probs);
                    } catch (Exception e) {
                        logger.error("Erreur dans processOutput", e);
                        throw new RuntimeException("Traitement de sortie échoué", e);
                    }
                }

                @Override
                public Batchifier getBatchifier() {
                    return null;
                }
            };

            Criteria<Image, Classifications> criteria = Criteria.builder()
                    .setTypes(Image.class, Classifications.class)
                    .optModelPath(modelResource.getFile().toPath())
                    .optTranslator(translator)
                    .optEngine("PyTorch")
                    .optDevice(Device.cpu())
                    .build();

            model = ModelZoo.loadModel(criteria);
            predictor = model.newPredictor();

            logger.info("✅ Modèle CIFAR-10 chargé avec succès!");
            logger.info("✅ Taille d'entrée: {}x{}", modelConfig.getInputSize(), modelConfig.getInputSize());

            // Test rapide
            testWithDummyImage();

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du modèle", e);
            throw e;
        }
    }

    private void testWithDummyImage() {
        try {
            try (ai.djl.ndarray.NDManager manager = ai.djl.ndarray.NDManager.newBaseManager()) {
                int inputSize = modelConfig.getInputSize();
                // Créer une image factice 64x64 RGB
                NDArray dummy = manager.randomUniform(0, 1, new Shape(inputSize, inputSize, 3));
                Image dummyImage = ImageFactory.getInstance().fromNDArray(dummy);

                Classifications result = predictor.predict(dummyImage);
                logger.info("✅ Test réussi! Prédiction: {} ({:.2f}%)",
                        result.best().getClassName(), result.best().getProbability() * 100);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Test échoué: {}", e.getMessage());
        }
    }

    public Map<String, Object> predict(byte[] imageBytes) {
        try {
            // Charger l'image depuis les bytes
            Image img;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                img = ImageFactory.getInstance().fromInputStream(bais);
            }

            if (img == null) {
                throw new RuntimeException("Échec du chargement de l'image depuis les bytes");
            }

            logger.info("📸 Image chargée: {}x{} (sera redimensionnée à {}x{})",
                    img.getWidth(), img.getHeight(),
                    modelConfig.getInputSize(), modelConfig.getInputSize());

            Classifications result = predictor.predict(img);
            logger.info("✅ Prédiction réussie! Classe: {} ({:.2f}%)",
                    result.best().getClassName(),
                    result.best().getProbability() * 100);   return formatPrediction(result);
        } catch (Exception e) {
            logger.error("❌ Prédiction échouée", e);
            throw new RuntimeException("Prédiction échouée: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> formatPrediction(Classifications classifications) {
        var best = classifications.best();
        List<String> classes = modelConfig.getClasses();

        List<Map<String, Object>> top3 = classifications.topK(3).stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("class", c.getClassName());
                    map.put("confidence", c.getProbability());
                    map.put("class_id", classes.indexOf(c.getClassName()));
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Double> allProbs = classifications.items().stream()
                .collect(Collectors.toMap(
                        Classifications.Classification::getClassName,
                        Classifications.Classification::getProbability
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("prediction", best.getClassName());
        result.put("confidence", best.getProbability());
        result.put("class_id", classes.indexOf(best.getClassName()));
        result.put("top_3", top3);
        result.put("all_probabilities", allProbs);

        return result;
    }

    @PreDestroy
    public void cleanup() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        logger.info("Modèle PyTorch nettoyé");
    }
}