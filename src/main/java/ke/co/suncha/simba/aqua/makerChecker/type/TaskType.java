package ke.co.suncha.simba.aqua.makerChecker.type;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha on 11/16/16.
 */
@Entity
@Table(name = "task_types")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskType implements Serializable {
    @Id
    @Column(name = "task_type_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taskTypeId;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    public Long getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(Long taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
