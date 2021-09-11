package tw.codethefuckup.multijpa.repository.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.codethefuckup.multijpa.entity.primary.MercedesBenz;

/**
 * 賓士
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
@Repository
public interface MercedesRepository extends JpaRepository<MercedesBenz, Integer> {
}
