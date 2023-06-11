package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	//@Autowired         // @Autowired 를 써도 되고,
	@PersistenceContext  // Java 표준 스펙
			EntityManager em;

	@Test
	@Rollback(false)
	void contextLoads() {
		Hello hello = new Hello();
		hello.setName("Anakin");
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("H");

		em.flush();
		em.clear();

		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
        //lombok 동작 확인 (hello.getId())
		assertThat(result.getId()).isEqualTo(hello.getId());

		//System.out.println("result = [" + result.getId()+"], "+result.getName());
	}

}
