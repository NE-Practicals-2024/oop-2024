package rw.bnr.banking.v1.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import rw.bnr.banking.v1.audits.TimestampAudit;
import rw.bnr.banking.v1.enums.ETransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "banking_transaction")
public class BankingTransaction extends TimestampAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_to_id")
    private Customer receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "amount")
    private double amount;

    @Column(name = "account")
    private String account;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ETransactionType transactionType;

    @CreationTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Column(name="banking_date_time")
    private LocalDateTime bankingDateTime;

}
