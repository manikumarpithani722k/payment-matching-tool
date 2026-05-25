export type MatchStatus = 'MATCHED' | 'ONLYSYSTEM' | 'ONLYPROVIDER' | 'AMOUNTMISMATCH';
export type ResolutionSide = 'SYSTEM' | 'PROVIDER';
export type ResultFilter = 'UNRESOLVED' | 'RESOLVED' | 'ALL';

export interface MatchSummary {
  total: number;
  matched: number;
  onlySystem: number;
  onlyProvider: number;
  amountMismatch: number;
}

export interface MatchResult {
  id: number;
  orderId: string;
  systemAmount: number | null;
  providerAmount: number | null;
  currency: string;
  status: MatchStatus;
  resolved: boolean;
  resolutionSide: ResolutionSide | null;
  createdAt: string;
}

export interface MatchRunResponse {
  summary: MatchSummary;
  results: MatchResult[];
}
