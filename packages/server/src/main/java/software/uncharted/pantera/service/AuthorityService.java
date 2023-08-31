package software.uncharted.pantera.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.pantera.model.authority.Authority;
import software.uncharted.pantera.model.authority.AuthorityType;
import software.uncharted.pantera.repository.AuthorityRepository;

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
