let accessToken: string | null = null;
let accessTokenType: string | null = null;

export function setAuthTokens(nextAccessToken: string, nextAccessTokenType: string = 'Bearer') {
      accessToken = nextAccessToken;
      accessTokenType = nextAccessTokenType;
}

export function clearAuthTokens() {
      accessToken = null;
      accessTokenType = null;
}

export function getAccessToken() {
      return accessToken;
}

export function getAccessTokenType() {
      return accessTokenType;
}
