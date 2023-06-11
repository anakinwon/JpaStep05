package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")   // The following 1 profile is active: "local"
@Component
@RequiredArgsConstructor
public class InitMember {
    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        // 어플리케이션 로딩 시 샘플 데이터 추가
        //    - DB초기화 하기
        //    - 팀생성   : TEAMA, TEAMB
        //    - 회원생성 : 100명 생성, ID 짝수는 TEAMA, 홀수는 TEAMB
        @Transactional
        public void init() {
            Team teamA = new Team("ATEAM");
            Team teamB = new Team("BTEAM");

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 1; i < 101; i++) {
                Team selectTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i + 10, selectTeam));
            }

        }
    }
}
