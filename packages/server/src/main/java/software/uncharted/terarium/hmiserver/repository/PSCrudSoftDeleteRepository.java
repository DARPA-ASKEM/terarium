package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PSCrudSoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {
	List<T> findAllByIdInAndDeletedOnIsNull(final List<ID> ids);

	Optional<T> getByIdAndDeletedOnIsNull(final ID id);

	Page<T> findAllByPublicAssetIsTrueAndTemporaryIsFalseAndDeletedOnIsNull(final Pageable pageable);
}
