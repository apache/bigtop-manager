package org.apache.bigtop.manager.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "\"chat_message\"")
@TableGenerator(
        name = "chat_message_generator",
        table = "sequence",
        pkColumnName = "seq_name",
        valueColumnName = "seq_count")
public class ChatMessagePO extends BasePO {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "chat_threads_generator")
    @Column(name = "id")
    private Long id;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "sender")
    private String sender;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserPO userPO;

    @ManyToOne
    @JoinColumn(name = "thread_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ChatThreadPO chatThreadPO;
}
