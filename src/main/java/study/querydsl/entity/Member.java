package study.querydsl.entity;

import lombok.*;
import javax.persistence.*;

@Entity @Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of={"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    //연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;

    // 생성자 미리 구축
    public Member(String username) {
        this.username = username;
        this.age      = 0;
    }
    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team!=null) {
            changeTeam(team);
        }
    }

    // 팀이 바뀌면, 해당 팀 회원 정보도 바꿔준다.
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }


}
