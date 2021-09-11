package tw.codethefuckup.multijpa.repository.secondary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.codethefuckup.multijpa.entity.secondary.BayerischeMotorenWerke;

/**
 * 寶馬
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
@Repository
public interface BmwRepository extends JpaRepository<BayerischeMotorenWerke, Integer> {
}
