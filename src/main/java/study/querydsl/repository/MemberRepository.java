package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>
        , MemberRepositoryCustom              // 사용자정의 지원
        , QuerydslPredicateExecutor<Member>   // 인터페이스 지원
{

    /**
     *   <스프링 데이터 JPA 리포지토리 전략>
     *       - 간단한 정적쿼리는 이름으로 매핑해서 자동 생성해 준다.
     *       - 스프링 데이터 JPA가 메소드이름(findByUsername)을 가지고 자동으로 JPQL을 만들어 준다.
     *       - 메소드이름 분석해서 자동 생성시켜 줌
     *           : select m from Member m where m.username = :username 으로
     * */
    List<Member> findByUsername(String username);

    /**
     *   <스프링 데이터 JPA 리포지토리 전략>
     *       - 간단한 정적쿼리는 이름으로 매핑해서 자동 생성해 준다.
     *       - 스프링 데이터 JPA가 메소드이름(findByUsername)을 가지고 자동으로 JPQL을 만들어 준다.
     *       - 메소드이름 분석해서 자동 생성시켜 줌
     *           : select t from Team t where t.name = :name 으로
     * */
    List<Member> findByTeamName(String name);

}
