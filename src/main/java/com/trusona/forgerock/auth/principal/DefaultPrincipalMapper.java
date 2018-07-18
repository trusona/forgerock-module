package com.trusona.forgerock.auth.principal;

import com.trusona.client.TrusonaClient;
import com.trusona.forgerock.auth.TrusonaDebug;
import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

public class DefaultPrincipalMapper implements PrincipalMapper {
  static final String TRUSONA_APP_PREFIX = "trusonaId:";

  private final TrusonaAppPrincipalMapper appMapper;
  private final TrusonaSdkPrincipalMapper sdkMapper;

  public DefaultPrincipalMapper(TrusonaClient trusonaClient, IdentityFinder identityFinder) {
    this(new TrusonaAppPrincipalMapper(trusonaClient, identityFinder), new TrusonaSdkPrincipalMapper());
  }

  public DefaultPrincipalMapper(TrusonaAppPrincipalMapper appMapper, TrusonaSdkPrincipalMapper sdkMapper) {
    this.appMapper = appMapper;
    this.sdkMapper = sdkMapper;
  }

  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    TrusonaDebug.getInstance().message("Mapping Result to Principal: {}", result);
    Date lastAllowedExpiration = new Date(System.currentTimeMillis() - 60 * 1000);

    return Optional.of(result)
      .filter(TrusonaficationResult::isSuccessful)
      .filter(trusonaficationResult -> trusonaficationResult.getExpiresAt().after(lastAllowedExpiration))
      .map(TrusonaficationResult::getUserIdentifier)
      .flatMap(userIdentifier -> mapUserIdentifier(userIdentifier, result));
  }

  private Optional<Principal> mapUserIdentifier(String userIdentifier, TrusonaficationResult result) {
    if (userIdentifier.startsWith(TRUSONA_APP_PREFIX)) {
      return appMapper.mapPrincipal(result);
    }
    else {
      return sdkMapper.mapPrincipal(result);
    }
  }
}