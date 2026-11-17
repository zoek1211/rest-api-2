package com.back.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 모든 엔티티가 공통으로 가지는 필드(id, 생성일, 수정일)를 모아둔 부모 클래스입니다.
 *
 * <p>글(Post)과 댓글(PostComment)이 이 클래스를 상속합니다.
 * 같은 필드를 엔티티마다 반복해서 쓰지 않기 위해서입니다.
 *
 * <ul>
 *   <li>{@code @MappedSuperclass}: 이 클래스 자체는 테이블이 아니고,
 *       필드만 자식 엔티티의 테이블 컬럼으로 물려줍니다. (post 테이블에 id, create_date, modify_date 컬럼이 생김)</li>
 *   <li>{@code @EntityListeners(AuditingEntityListener.class)}: 저장/수정 직전에 스프링이 끼어들어
 *       {@code @CreatedDate}, {@code @LastModifiedDate} 필드에 현재 시각을 넣어 줍니다.
 *       (BackApplication 의 {@code @EnableJpaAuditing} 과 짝)</li>
 *   <li>{@code abstract}: 이 클래스 단독으로는 객체를 만들 일이 없으므로 추상 클래스로 둡니다.</li>
 * </ul>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter // 롬복: 모든 필드의 getter(getId(), getCreateDate() ...)를 컴파일 시점에 만들어 줌
public abstract class BaseEntity {
    /**
     * 기본키(PK).
     * {@code IDENTITY} 전략: DB 의 auto_increment 로 번호를 매김 (1, 2, 3 ...).
     * 그래서 INSERT 가 실행되어야 id 가 정해집니다.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    /** 처음 저장된 시각. 한 번 정해지면 바뀌지 않음. */
    @CreatedDate
    private LocalDateTime createDate;

    /** 마지막으로 수정된 시각. 처음 저장(INSERT)될 때 채워지고, 이후 UPDATE 될 때마다 갱신됨. */
    @LastModifiedDate
    private LocalDateTime modifyDate;

    /**
     * 두 엔티티가 "같은 데이터"인지 비교합니다.
     *
     * <p>기본 equals 는 메모리 주소를 비교하므로, DB 의 같은 행을 가리키는 객체라도
     * 다른 객체로 판단할 수 있습니다. 그래서 "클래스가 같고 id 가 같으면 같다" 로 정의합니다.
     *
     * <p>주의할 점:
     * <ul>
     *   <li>아직 저장되지 않은 엔티티는 id 가 모두 0 입니다. 이때 id 로 비교하면 서로 다른 새 댓글들이
     *       "같다"고 판단되므로, id 가 0 이면 같은 객체(o == this)일 때만 같다고 봅니다.</li>
     *   <li>{@code getClass()} 로 비교하므로, 지연 로딩(LAZY) 때 하이버네이트가 만드는 프록시 객체와
     *       실제 엔티티는 같은 행이어도 다르다고 판단됩니다. 이 프로젝트에서는 프록시끼리 비교할 일이 없어 그대로 둡니다.</li>
     * </ul>
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        // 저장 전(id == 0)인 엔티티는 위의 o == this 가 아니면 다른 것으로 봄
        if (id == 0 || that.id == 0) return false;
        return id == that.id;
    }

    /**
     * equals 를 재정의하면 hashCode 도 같은 기준(id)으로 재정의해야 HashSet/HashMap 에서 올바르게 동작합니다.
     *
     * <p>단, 저장 전(id 0)에 HashSet 에 넣은 엔티티는 저장 후 id 가 바뀌면 hashCode 도 바뀌어 찾을 수 없게 됩니다.
     * 그래서 엔티티는 저장 전에 HashSet/HashMap 의 키로 쓰지 않습니다. (글의 댓글 목록은 List 라서 상관없음)
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
