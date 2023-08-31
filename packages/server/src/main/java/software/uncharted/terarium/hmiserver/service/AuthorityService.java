package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.repository.AuthorityRepository;
import software.uncharted.terarium.hmiserver.model.authority.Authority;
import software.uncharted.terarium.hmiserver.model.authority.AuthorityType;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorityService {
  private final AuthorityRepository authorityRepository;

  public long count() {
    return authorityRepository.count();
  }

  public Authority createAuthority(final AuthorityType type) {
    return authorityRepository.save(new Authority().setName(type.name()));
  }
}
