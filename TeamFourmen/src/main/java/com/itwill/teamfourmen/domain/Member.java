package com.itwill.teamfourmen.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Basic;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder
@Getter
@Setter
@ToString
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "member")
public class Member {

	    @Id // PK
	    @EqualsAndHashCode.Include
	    private String email;
	    
	    @Basic(optional = false)
	    private String password;
	    
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long memberId;
	    
	    @Basic(optional = false)
	    private String name;
	    
	    @Basic(optional = false)
	    private String nickname;
	    
	    @Basic(optional = false)
	    private String phone;
	    
	    @Basic(optional = true)
	    private String type;
	    
	    @CreatedDate
	    private LocalDateTime created_time;
	    
	    @Basic(optional = true)
	    private String usersaveprofile;
	    
	    @Basic(optional = true)
	    private String useruploadprofile;
	    
	    @Builder.Default // Builder에서도 null이 아닌 Set<> 객체를 생성하기 위해서.
	    @ToString.Exclude
	    @ElementCollection(fetch = FetchType.LAZY) // 연관된 별도의 테이블을 사용.
	    @Enumerated(EnumType.STRING) // DB 테이블에 저장할 때 상수 이름(문자열)을 사용.
	    private Set<MemberRole> roles = new HashSet<>();

	    public Member addRole(MemberRole role) {
	        roles.add(role);
	        return this;
	    }
	    
	    public Member clearRoles() {
	        roles.clear();
	        return this;
	    }
	   
	    
	    public Member update(String email, String name, String nickname, String phone, String usersaveprofile) {
	        this.email = email;
	        this.name = name;
	        this.nickname = nickname;
	        this.phone= phone;
	        this.usersaveprofile = usersaveprofile;
	        
	        return this;
	    }
	    
	    public Member updatewithout(String email, String name, String nickname, String phone) {
	        this.email = email;
	        this.name = name;
	        this.nickname = nickname;
	        this.phone= phone;

	        
	        return this;
	    }


		public LocalDate getLocalDate(LocalDateTime created_time) {
			return created_time.toLocalDate();
		}

		@ToString.Exclude
		@OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY)
		@JsonIgnore
		private List<Follow> followings;

		@ToString.Exclude
		@OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY)
		@JsonIgnore
		private List<Follow> followers;

		@ToString.Exclude
		@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
		@JsonIgnore
		private List<Review> reviews;

		public String getRolesTargetUser() {
			return roles.stream().map(MemberRole::name).collect(Collectors.joining(", "));
		}

}
