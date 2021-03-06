package snma.game.pong.messages

import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.network.AbstractMessage
import com.jme3.network.serializing.Serializable
import com.jme3.network.serializing.Serializer
import snma.game.pong.Constants

object Messages {
    fun register() {
        Serializer.registerClass(PhysicsStateMessage::class.java)
        Serializer.registerClass(CountdownMessage::class.java)
        Serializer.registerClass(ClientMovedMessage::class.java)
        Serializer.registerClass(WallCollisionMessage::class.java)
        Serializer.registerClass(PlayerCollisionMessage::class.java)
        Serializer.registerClass(ScoreIncreaseMessage::class.java)
    }
}

@Serializable
data class PhysicsState(var pos: Vector2f, var rot: Float, var linearVel: Vector2f, var angularVel: Float) {
    @Suppress("unused")
    constructor(): this(pos = EMPTY.pos, rot = EMPTY.rot, linearVel = EMPTY.linearVel, angularVel = EMPTY.angularVel)
    companion object {
        val EMPTY = PhysicsState(
            pos = Vector2f.ZERO,
            rot = 0f,
            linearVel = Vector2f.ZERO,
            angularVel = 0f,
        )

        fun ofRigidBody(control: RigidBodyControl, invert: Boolean): PhysicsState {
            var angle = control.physicsRotation.toAngleAxis(tmpVec)
            if (tmpVec.z < 0f) {
                angle *= -1f
            }
            return if (!invert) {
                PhysicsState(
                    pos = Vector2f(control.physicsLocation.x, control.physicsLocation.y),
                    rot = angle,
                    linearVel = Vector2f(control.linearVelocity.x, control.linearVelocity.y),
                    angularVel = control.angularVelocity.z,
                )
            } else {
                PhysicsState(
                    pos = Vector2f(control.physicsLocation.x, Constants.SCREEN_HEIGHT - control.physicsLocation.y),
                    rot = -angle,
                    linearVel = Vector2f(control.linearVelocity.x, -control.linearVelocity.y),
                    angularVel = -control.angularVelocity.z,
                )
            }
        }
    }

    fun applyToRigidBody(control: RigidBodyControl) {
        val _pos = control.physicsLocation
        _pos.x = pos.x
        _pos.y = pos.y
        _pos.z = 0f
        control.physicsLocation = _pos

        val _rot = control.physicsRotation
        _rot.fromAngleAxis(rot, Vector3f.UNIT_Z)
        control.physicsRotation = _rot

        val _linearVel = control.linearVelocity
        _linearVel.x = linearVel.x
        _linearVel.y = linearVel.y
        _linearVel.z = 0f
        control.linearVelocity = _linearVel

        val _angularVel = control.angularVelocity
        _angularVel.x = 0f
        _angularVel.y = 0f
        _angularVel.z = angularVel
        control.angularVelocity = _angularVel
    }
}
private val tmpVec = Vector3f()

@Serializable
class PhysicsStateMessage(
    var you: Float,
    var enemy: Float,
    var ball: PhysicsState,
) : AbstractMessage() {
    @Suppress("unused")
    constructor(): this(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, PhysicsState.EMPTY)
}

@Serializable
class CountdownMessage(var value: Int): AbstractMessage() {
    init {
        isReliable = true
    }

    @Suppress("unused")
    constructor() : this(Int.MIN_VALUE)
}

@Serializable
class ClientMovedMessage(var newPosition: Float) : AbstractMessage() {
    @Suppress("unused")
    constructor(): this(Float.POSITIVE_INFINITY)
}

sealed class EventMessage(var position: Vector2f): AbstractMessage() {
    init {
        isReliable = true
    }
}

@Serializable
class WallCollisionMessage(position: Vector2f): EventMessage(position) {
    @Suppress("unused")
    constructor(): this(Vector2f.ZERO)
}

@Serializable
class PlayerCollisionMessage(position: Vector2f, var isYou: Boolean): EventMessage(position) {
    @Suppress("unused")
    constructor(): this(Vector2f.ZERO, false)
}

@Serializable
class ScoreIncreaseMessage(
    position: Vector2f, var yourNewScore: Int, var enemyNewScore: Int, var yourScoreIncreased: Boolean
) : EventMessage(position) {
    @Suppress("unused")
    constructor(): this(Vector2f.ZERO, 0, 0, false)
}