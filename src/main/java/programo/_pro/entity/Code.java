//package programo._pro.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Table(name = "code")
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Data
//public class Code {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @Column(name = "test_id", nullable = false)
//    private Test test;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @Column(name = "problem_id", nullable = false)
//    private Problem problem;
//
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @Column(name = "user_id", nullable = false)
//    private User user;
//
//
//
//}
