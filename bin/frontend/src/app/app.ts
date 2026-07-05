import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  MatchResult,
  MatchRunResponse,
  MatchSummary,
  ResolutionSide,
  ResultFilter
} from './payment-match.models';
import { PaymentMatchService } from './payment-match.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  systemFile: File | null = null;
  providerFile: File | null = null;
  filter: ResultFilter = 'UNRESOLVED';
  summary: MatchSummary | null = null;
  results: MatchResult[] = [];
  errorMessage = '';
  successMessage = '';
  loading = false;
  resolvingId: number | null = null;

  readonly filters: ResultFilter[] = ['UNRESOLVED', 'RESOLVED', 'ALL'];

  constructor(private readonly paymentMatchService: PaymentMatchService) {}

  onSystemFileSelected(event: Event): void {
    this.systemFile = this.getSelectedFile(event);
  }

  onProviderFileSelected(event: Event): void {
    this.providerFile = this.getSelectedFile(event);
  }

  runMatch(): void {
    this.clearMessages();

    if (!this.systemFile || !this.providerFile) {
      this.errorMessage = 'Please select both System CSV and Provider CSV before running the match.';
      return;
    }

    const fileValidationMessage = this.validateSelectedFiles();
    if (fileValidationMessage) {
      this.errorMessage = fileValidationMessage;
      return;
    }

    this.loading = true;
    this.paymentMatchService
      .runMatch(this.systemFile, this.providerFile)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response: MatchRunResponse) => {
          this.summary = response.summary;
          this.results = this.applyCurrentFilter(response.results);
          this.successMessage = 'Match run completed successfully.';
          if (this.filter !== 'ALL') {
            this.loadResults();
          }
        },
        error: (error) => {
          this.errorMessage = error?.error?.message || 'Upload or processing failed.';
        }
      });
  }

  loadResults(): void {
    this.clearMessages(false);
    this.loading = true;

    this.paymentMatchService
      .getResults(this.filter)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (results) => {
          this.results = results;
        },
        error: (error) => {
          this.errorMessage = error?.error?.message || 'Unable to load match results.';
        }
      });
  }

  resolve(result: MatchResult, resolutionSide: ResolutionSide): void {
    this.clearMessages(false);
    this.resolvingId = result.id;

    this.paymentMatchService
      .resolve(result.id, resolutionSide)
      .pipe(finalize(() => (this.resolvingId = null)))
      .subscribe({
        next: (updatedResult) => {
          if (this.filter === 'UNRESOLVED') {
            this.results = this.results.filter((item) => item.id !== updatedResult.id);
          } else {
            this.results = this.results.map((item) =>
              item.id === updatedResult.id ? updatedResult : item
            );
          }
          this.successMessage = `${updatedResult.orderId} resolved with ${resolutionSide}.`;
        },
        error: (error) => {
          this.errorMessage = error?.error?.message || 'Unable to resolve this row.';
        }
      });
  }

  statusLabel(status: string): string {
    return status
      .replace('ONLYSYSTEM', 'Only System')
      .replace('ONLYPROVIDER', 'Only Provider')
      .replace('AMOUNTMISMATCH', 'Amount Mismatch')
      .replace('MATCHED', 'Matched');
  }

  private getSelectedFile(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    return input.files?.item(0) ?? null;
  }

  private clearMessages(clearSuccess = true): void {
    this.errorMessage = '';
    if (clearSuccess) {
      this.successMessage = '';
    }
  }

  private validateSelectedFiles(): string {
    const systemFileName = this.systemFile?.name.toLowerCase() || '';
    const providerFileName = this.providerFile?.name.toLowerCase() || '';

    if (!systemFileName.endsWith('.csv') || !providerFileName.endsWith('.csv')) {
      return 'Please upload CSV files only.';
    }

    if (!systemFileName.includes('system')) {
      return 'Please upload the proper System CSV document in the System CSV field.';
    }

    if (!providerFileName.includes('provider')) {
      return 'Please upload the proper Provider CSV document in the Provider CSV field.';
    }

    return '';
  }

  private applyCurrentFilter(results: MatchResult[]): MatchResult[] {
    if (this.filter === 'ALL') {
      return results;
    }
    const resolved = this.filter === 'RESOLVED';
    return results.filter((result) => result.resolved === resolved);
  }
}
