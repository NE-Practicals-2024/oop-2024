package rw.bnr.banking.v1.audits;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"createdBy", "updatedBy"}, allowGetters = true)
public abstract class InitiatorAudit extends TimestampAudit{
    private static final Long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;


    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;
}
