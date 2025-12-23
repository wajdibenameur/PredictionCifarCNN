import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
export interface PredictionResponse {
  prediction: string;
  confidence: number;
  class_id: number;
  top_3: Array<{class: string, confidence: number, class_id: number}>;
  all_probabilities: {[key: string]: number};
  filename?: string;
  size?: number;
  source_url?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PredictionService {
  private apiUrl = environment.apiBaseUrl + '/predict';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token'); // Assure-toi de stocker le token à la connexion
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  predictSingle(image: File): Observable<PredictionResponse> {
    const formData = new FormData();
    formData.append('image', image);

    return this.http.post<PredictionResponse>(
      `${this.apiUrl}/single`,
      formData,
      { headers: this.getAuthHeaders() }  // <-- JWT ajouté ici
    );
  }

  predictFromUrl(url: string): Observable<PredictionResponse> {
    return this.http.post<PredictionResponse>(
      `${this.apiUrl}/url`,
      { url },
      { headers: this.getAuthHeaders() } // <-- JWT ajouté ici
    );
  }

  healthCheck(): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/health`,
      { headers: this.getAuthHeaders() } // <-- JWT ajouté ici si nécessaire
    );
  }
}
