
package tech.kiwa.engine.component;

import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RuleItem;

public abstract class AbstractCommand {
    public abstract void execute(RuleItem item, ItemExecutedResult result);

    public abstract void SetObject(Object obj);
}
