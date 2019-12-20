package tech.kiwa.engine.component.drools;

public interface DeclareInterface extends DroolsPartsObject {
    Object getValue(String name);

    void setValue(String name, Object value);
}
