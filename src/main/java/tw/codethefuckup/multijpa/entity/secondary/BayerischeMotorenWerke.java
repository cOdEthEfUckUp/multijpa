package tw.codethefuckup.multijpa.entity.secondary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 寶馬
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
@Data
@Entity
@SuppressWarnings("PersistenceUnitPresent")
@Table(name = "bayerischemotoren_werke", schema = "", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"model"})
})
public class BayerischeMotorenWerke implements Serializable {

	private static final long serialVersionUID = 6327640726291276821L;

	@Basic(optional = false)
	@Column(nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Integer id;

	@Basic(optional = false)
	@Column(nullable = false)
	private String model;

	@Override
	public String toString() {
		try {
			return new JsonMapper().writeValueAsString(this);
		} catch (JsonProcessingException ignore) {
			return Objects.isNull(id) ? "null" : id.toString();
		}
	}
}
