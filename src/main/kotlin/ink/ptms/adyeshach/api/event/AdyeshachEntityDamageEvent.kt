package ink.ptms.adyeshach.api.event

import ink.ptms.adyeshach.common.entity.EntityInstance
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyEvent

/**
 * @Author sky
 * @Since 2020-08-14 19:21
 */
class AdyeshachEntityDamageEvent(val entity: EntityInstance, val player: Player) : ProxyEvent()