package ink.ptms.adyeshach.compat.modelengine4

import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes
import com.ticxo.modelengine.core.animation.handler.PriorityHandler
import com.ticxo.modelengine.core.animation.handler.StateMachineHandler
import com.ticxo.modelengine.v1_20_R3.NMSHandler_v1_20_R3
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.AdyeshachEntityTypeRegistry
import ink.ptms.adyeshach.core.entity.EntityTypes
import ink.ptms.adyeshach.core.entity.ModelEngine
import ink.ptms.adyeshach.core.entity.ModelEngineOptions
import ink.ptms.adyeshach.impl.entity.DefaultEntityInstance
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.util.unsafeLazy


/**
 * Adyeshach
 * ink.ptms.adyeshach.compat.modelengine2.DefaultModelEngine
 *
 * @author 坏黑
 * @since 2022/6/19 21:58
 */
@Suppress("SpellCheckingInspection")
internal interface DefaultModelEngine : ModelEngine {

    override fun showModelEngine(viewer: Player): Boolean {
        if (isModelEngineHooked) {
            // 初始化模型
            if (modelEngineName.isNotBlank() && modelEngineUniqueId == null) {
                refreshModelEngine()
            }
        }
        return true
    }

    override fun hideModelEngine(viewer: Player): Boolean {
        return true
    }

    override fun refreshModelEngine(): Boolean {
        if (isModelEngineHooked) {
            this as DefaultEntityInstance

            // 创建模型
            if (modelEngineName.isNotBlank()) {
                modelEngineUniqueId = normalizeUniqueId

                ModelEngineAPI.getModeledEntity(normalizeUniqueId)?.destroy()

                // 先销毁原版实体，再创建模型
                despawn()

                // 创建代理实体
                val entity = EntityModeled(this)
                entity.syncLocation(getLocation())

                // 创建模型
                val model = ModelEngineAPI.getOrCreateModeledEntity(normalizeUniqueId) { entity }
                model.setSaved(true)
                model.isBaseEntityVisible = false

                // 私有模型兼容
                if (!isPublic()) {
                    entity.isDetectingPlayers = false
                    forViewers { t -> entity.setForceViewing(t, true) }
                }

                // 获取配置
                val options = modelEngineOptions ?: ModelEngineOptions()
                // 没有模型
                val activeModel = ModelEngineAPI.createActiveModel(modelEngineName, null) {
                    if (options.useStateMachine) StateMachineHandler(it) else PriorityHandler(it)
                }
                // 应用配置
                options.apply(activeModel)
                // 添加模型
                model.addModel(activeModel, options.isOverrideHitbox)

                // 更新名称
                updateModelEngineNameTag()
            }
            // 销毁模型
            else {
                ModelEngineAPI.getModeledEntity(normalizeUniqueId)?.destroy()
                respawn()
            }
        }
        return false
    }

    override fun updateModelEngineNameTag() {
        this as DefaultEntityInstance
        val modeledEntity = ModelEngineAPI.getModeledEntity(modelEngineUniqueId ?: return) ?: return
        modeledEntity.models.values.forEach { model ->
            model.getBone("nametag").flatMap { it.getBoneBehavior(BoneBehaviorTypes.NAMETAG) }.ifPresent { nameTag ->
                // 名称可见
                if (isCustomNameVisible()) {
                    nameTag.isVisible = true
                } else {
                    nameTag.isVisible = false
                    return@ifPresent
                }
                // 名称内容
                nameTag.jsonString = getCustomNameRaw()
            }
        }
    }

    override fun hurt() {
    }

    companion object {

        val isModelEngineHooked by unsafeLazy {
            (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) && kotlin.runCatching { NMSHandler_v1_20_R3::class.java }.isSuccess
        }

        @Awake(LifeCycle.LOAD)
        fun init() {
            // 注册生成回调
            Adyeshach.api().getEntityTypeRegistry().prepareGenerate(object : AdyeshachEntityTypeRegistry.GenerateCallback {

                override fun invoke(entityType: EntityTypes, interfaces: List<String>): List<String> {
                    val array = ArrayList<String>()
                    // 是否安装 ModelEngine 扩展
                    if (isModelEngineHooked) {
                        array += DefaultModelEngine::class.java.name
                    }
                    return array
                }
            })
        }
    }
}