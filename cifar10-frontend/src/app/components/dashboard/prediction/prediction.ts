import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PredictionService,PredictionResponse } from '../../../services/prediction-service';

@Component({
  selector: 'app-prediction',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prediction.html',
  styleUrls: ['./prediction.css']
})
export class Prediction {
  selectedModel = 'cifar10-cnn';
  imageFile: File | null = null;
  imageUrl: string = '';
  imagePreview: string | ArrayBuffer | null = null;
  predictionResult: PredictionResponse | null = null;
  errorMessage: string = '';
  isLoading: boolean = false;
  useUrl: boolean = false;

  constructor(private predictionService: PredictionService) {}

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      if (!file.type.match('image.*')) {
        this.errorMessage = 'Veuillez sélectionner un fichier image.';
        return;
      }
      
      this.imageFile = file;
      this.imageUrl = '';
      this.predictionResult = null;
      this.errorMessage = '';
      
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }

  onPredict(): void {
    if (this.useUrl) {
      if (!this.imageUrl) {
        this.errorMessage = 'Veuillez entrer une URL d\'image.';
        return;
      }
      
      this.isLoading = true;
      this.predictionService.predictFromUrl(this.imageUrl).subscribe({
        next: (response) => {
          this.predictionResult = response;
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = error.error?.error || 'Erreur de prédiction';
          this.isLoading = false;
        }
      });
    } else {
      if (!this.imageFile) {
        this.errorMessage = 'Veuillez sélectionner une image.';
        return;
      }
      
      this.isLoading = true;
      this.predictionService.predictSingle(this.imageFile).subscribe({
        next: (response) => {
          this.predictionResult = response;
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = error.error?.error || 'Erreur de prédiction';
          this.isLoading = false;
        }
      });
    }
  }

  toggleInputMethod(): void {
    this.useUrl = !this.useUrl;
    this.imageFile = null;
    this.imageUrl = '';
    this.imagePreview = null;
    this.predictionResult = null;
    this.errorMessage = '';
  }

  getClassColor(className: string): string {
    const colors: {[key: string]: string} = {
      'airplane': 'primary',
      'automobile': 'success',
      'bird': 'warning',
      'cat': 'danger',
      'deer': 'info',
      'dog': 'secondary',
      'frog': 'success',
      'horse': 'warning',
      'ship': 'primary',
      'truck': 'danger'
    };
    return colors[className] || 'secondary';
  }
}