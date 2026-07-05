import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MatchResult, MatchRunResponse, ResolutionSide, ResultFilter } from './payment-match.models';

@Injectable({ providedIn: 'root' })
export class PaymentMatchService {
  private readonly apiUrl = 'http://localhost:8081/api/matches';

  constructor(private readonly http: HttpClient) {}

  runMatch(systemFile: File, providerFile: File): Observable<MatchRunResponse> {
    const formData = new FormData();
    formData.append('systemFile', systemFile);
    formData.append('providerFile', providerFile);

    return this.http.post<MatchRunResponse>(`${this.apiUrl}/run`, formData);
  }

  getResults(filter: ResultFilter): Observable<MatchResult[]> {
    return this.http.get<MatchResult[]>(this.apiUrl, {
      params: { filter }
    });
  }

  resolve(id: number, resolutionSide: ResolutionSide): Observable<MatchResult> {
    return this.http.patch<MatchResult>(`${this.apiUrl}/${id}/resolve`, { resolutionSide });
  }
}
