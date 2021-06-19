# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: edu/uci/ics/amber/engine/common/virtualidentity.proto, edu/uci/ics/amber/engine/common/ambermessage2.proto
# plugin: python-betterproto
import betterproto
from dataclasses import dataclass

from .edu.uci.ics.amber.engine.architecture import worker
from .google import protobuf


@dataclass
class ActorVirtualIdentity(betterproto.Message):
    """sealed trait ActorVirtualIdentity"""

    worker_actor_virtual_identity: "WorkerActorVirtualIdentity" = (
        betterproto.message_field(1, group="sealed_value")
    )
    controller_virtual_identity: "ControllerVirtualIdentity" = (
        betterproto.message_field(2, group="sealed_value")
    )
    self_virtual_identity: "SelfVirtualIdentity" = betterproto.message_field(
        3, group="sealed_value"
    )
    client_virtual_identity: "ClientVirtualIdentity" = betterproto.message_field(
        4, group="sealed_value"
    )


@dataclass
class WorkerActorVirtualIdentity(betterproto.Message):
    """
    final case class WorkerActorVirtualIdentity (    name: String ) extends
    ActorVirtualIdentity
    """

    name: str = betterproto.string_field(1)


@dataclass
class ControllerVirtualIdentity(betterproto.Message):
    """
    final case class ControllerVirtualIdentity extends ActorVirtualIdentity
    """

    pass


@dataclass
class SelfVirtualIdentity(betterproto.Message):
    """final case class SelfVirtualIdentity extends ActorVirtualIdentity"""

    pass


@dataclass
class ClientVirtualIdentity(betterproto.Message):
    """final case class ClientVirtualIdentity extends ActorVirtualIdentity"""

    pass


@dataclass
class LayerIdentity(betterproto.Message):
    """
    final case class LayerIdentity (    workflow: String,    operator: String,
    layerID: String )
    """

    workflow: str = betterproto.string_field(1)
    operator: str = betterproto.string_field(2)
    layer_i_d: str = betterproto.string_field(3)


@dataclass
class LinkIdentity(betterproto.Message):
    """
    final case class LinkIdentity (    from: LayerIdentity,    to:
    LayerIdentity )
    """

    from_: "LayerIdentity" = betterproto.message_field(1)
    to: "LayerIdentity" = betterproto.message_field(2)


@dataclass
class OperatorIdentity(betterproto.Message):
    """
    final case class LinkIdentity (    workflow: String,    operator: String )
    """

    workflow: str = betterproto.string_field(1)
    operator: str = betterproto.string_field(2)


@dataclass
class WorkflowIdentity(betterproto.Message):
    """final case class WorkflowIdentity (    id: String )"""

    id: str = betterproto.string_field(1)


@dataclass
class DataPayload(betterproto.Message):
    pass


@dataclass
class ControlInvocation(betterproto.Message):
    command_i_d: int = betterproto.int64_field(1)
    command: worker.ControlCommand = betterproto.message_field(2)


@dataclass
class ControlPayload(betterproto.Message):
    control_invocation: "ControlInvocation" = betterproto.message_field(
        1, group="sealed_value"
    )


@dataclass
class ReturnPayload(betterproto.Message):
    original_command_i_d: int = betterproto.int64_field(1)
    return_value: protobuf.Any = betterproto.message_field(2)


@dataclass
class WorkflowDataMessage(betterproto.Message):
    from_: "ActorVirtualIdentity" = betterproto.message_field(1)
    sequence_number: int = betterproto.int64_field(2)
    payload: "DataPayload" = betterproto.message_field(3)


@dataclass
class WorkflowControlMessage(betterproto.Message):
    from_: "ActorVirtualIdentity" = betterproto.message_field(1)
    sequence_number: int = betterproto.int64_field(2)
    payload: "ControlPayload" = betterproto.message_field(3)


@dataclass
class WorkflowMessage(betterproto.Message):
    """sealed trait WorkflowMessage"""

    workflow_data_message: "WorkflowDataMessage" = betterproto.message_field(
        1, group="sealed_value"
    )
    workflow_control_message: "WorkflowControlMessage" = betterproto.message_field(
        2, group="sealed_value"
    )
