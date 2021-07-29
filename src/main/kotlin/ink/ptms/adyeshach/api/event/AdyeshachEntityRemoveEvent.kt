package ink.ptms.adyeshach.api.event

import ink.ptms.adyeshach.common.entity.EntityInstance
import taboolib.common.platform.ProxyEvent

/**
 * @Author sky
 * @Since 2020-08-14 19:21
 */
class AdyeshachEntityRemoveEvent(val entity: EntityInstance) : ProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}