package tw.codethefuckup.multijpa.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tw.codethefuckup.multijpa.entity.primary.MercedesBenz;
import tw.codethefuckup.multijpa.entity.secondary.BayerischeMotorenWerke;
import tw.codethefuckup.multijpa.repository.secondary.BmwRepository;
import tw.codethefuckup.multijpa.repository.primary.MercedesRepository;

/**
 * 控制器
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
@RestController
@RequestMapping("/")
public class WelcomeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeController.class);

	@Autowired
	private BmwRepository bmwRepository;

	@Autowired
	private MercedesRepository mercedesRepository;

	@GetMapping("benz")
	@ResponseBody
	List<MercedesBenz> somethingFromPrimaryDataSource() {
		return mercedesRepository.findAll();
	}

	@GetMapping("bmw")
	@ResponseBody
	List<BayerischeMotorenWerke> somethingFromSecondaryDataSource() {
		return bmwRepository.findAll();
	}
}
