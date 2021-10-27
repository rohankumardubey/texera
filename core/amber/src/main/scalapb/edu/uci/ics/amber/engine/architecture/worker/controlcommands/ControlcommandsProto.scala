// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

object ControlcommandsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.StartWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PauseWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ResumeWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.UpdateInputLinkingV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.AddPartitioningV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.WorkerExecutionCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryStatisticsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryCurrentInputTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LocalOperatorExceptionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.InitializeOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ModifyOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ReplayCurrentTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonPrintV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.EvaluateExpressionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlCommandV2Message
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkJlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9sY29tbWFuZHMucHJvdG8SLGVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyGkdlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0Z
  WN0dXJlL3NlbmRzZW1hbnRpY3MvcGFydGl0aW9uaW5ncy5wcm90bxo1ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi92a
  XJ0dWFsaWRlbnRpdHkucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90byIPCg1TdGFydFdvcmtlclYyIg8KDVBhdXNlV29ya2VyV
  jIiEAoOUmVzdW1lV29ya2VyVjIi4gEKFFVwZGF0ZUlucHV0TGlua2luZ1YyEmkKCmlkZW50aWZpZXIYASABKAsyNS5lZHUudWNpL
  mljcy5hbWJlci5lbmdpbmUuY29tbW9uLkFjdG9yVmlydHVhbElkZW50aXR5QhLiPw8SCmlkZW50aWZpZXLwAQFSCmlkZW50aWZpZ
  XISXwoKaW5wdXRfbGluaxgCIAEoCzItLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uTGlua0lkZW50aXR5QhHiPw4SC
  WlucHV0TGlua/ABAVIJaW5wdXRMaW5rIt4BChFBZGRQYXJ0aXRpb25pbmdWMhJMCgN0YWcYASABKAsyLS5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuY29tbW9uLkxpbmtJZGVudGl0eUIL4j8IEgN0YWfwAQFSA3RhZxJ7CgxwYXJ0aXRpb25pbmcYAiABKAsyQS5lZ
  HUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnNlbmRzZW1hbnRpY3MuUGFydGl0aW9uaW5nQhTiPxESDHBhcnRpd
  GlvbmluZ/ABAVIMcGFydGl0aW9uaW5nIhwKGldvcmtlckV4ZWN1dGlvbkNvbXBsZXRlZFYyIhMKEVF1ZXJ5U3RhdGlzdGljc1YyI
  hoKGFF1ZXJ5Q3VycmVudElucHV0VHVwbGVWMiJCChhMb2NhbE9wZXJhdG9yRXhjZXB0aW9uVjISJgoHbWVzc2FnZRgBIAEoCUIM4
  j8JEgdtZXNzYWdlUgdtZXNzYWdlImYKGUluaXRpYWxpemVPcGVyYXRvckxvZ2ljVjISHQoEY29kZRgBIAEoCUIJ4j8GEgRjb2RlU
  gRjb2RlEioKCWlzX3NvdXJjZRgCIAEoCEIN4j8KEghpc1NvdXJjZVIIaXNTb3VyY2UiYgoVTW9kaWZ5T3BlcmF0b3JMb2dpY1YyE
  h0KBGNvZGUYASABKAlCCeI/BhIEY29kZVIEY29kZRIqCglpc19zb3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlI
  hYKFFJlcGxheUN1cnJlbnRUdXBsZVYyIjcKDVB5dGhvblByaW50VjISJgoHbWVzc2FnZRgBIAEoCUIM4j8JEgdtZXNzYWdlUgdtZ
  XNzYWdlIkcKFEV2YWx1YXRlRXhwcmVzc2lvblYyEi8KCmV4cHJlc3Npb24YASABKAlCD+I/DBIKZXhwcmVzc2lvblIKZXhwcmVzc
  2lvbiLgDwoQQ29udHJvbENvbW1hbmRWMhJyCgxzdGFydF93b3JrZXIYASABKAsyOy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY
  XJjaGl0ZWN0dXJlLndvcmtlci5TdGFydFdvcmtlclYyQhDiPw0SC3N0YXJ0V29ya2VySABSC3N0YXJ0V29ya2VyEnIKDHBhdXNlX
  3dvcmtlchgCIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlBhdXNlV29ya2VyVjJCE
  OI/DRILcGF1c2VXb3JrZXJIAFILcGF1c2VXb3JrZXISdgoNcmVzdW1lX3dvcmtlchgDIAEoCzI8LmVkdS51Y2kuaWNzLmFtYmVyL
  mVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlJlc3VtZVdvcmtlclYyQhHiPw4SDHJlc3VtZVdvcmtlckgAUgxyZXN1bWVXb3JrZ
  XISggEKEGFkZF9wYXJ0aXRpb25pbmcYBCABKAsyPy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlc
  i5BZGRQYXJ0aXRpb25pbmdWMkIU4j8REg9hZGRQYXJ0aXRpb25pbmdIAFIPYWRkUGFydGl0aW9uaW5nEo8BChR1cGRhdGVfaW5wd
  XRfbGlua2luZxgFIAEoCzJCLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlVwZGF0ZUlucHV0T
  Glua2luZ1YyQhfiPxQSEnVwZGF0ZUlucHV0TGlua2luZ0gAUhJ1cGRhdGVJbnB1dExpbmtpbmcSggEKEHF1ZXJ5X3N0YXRpc3RpY
  3MYBiABKAsyPy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5RdWVyeVN0YXRpc3RpY3NWMkIU4
  j8REg9xdWVyeVN0YXRpc3RpY3NIAFIPcXVlcnlTdGF0aXN0aWNzEqABChlxdWVyeV9jdXJyZW50X2lucHV0X3R1cGxlGAcgASgLM
  kYuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUXVlcnlDdXJyZW50SW5wdXRUdXBsZVYyQhviP
  xgSFnF1ZXJ5Q3VycmVudElucHV0VHVwbGVIAFIWcXVlcnlDdXJyZW50SW5wdXRUdXBsZRKfAQoYbG9jYWxfb3BlcmF0b3JfZXhjZ
  XB0aW9uGAggASgLMkYuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTG9jYWxPcGVyYXRvckV4Y
  2VwdGlvblYyQhviPxgSFmxvY2FsT3BlcmF0b3JFeGNlcHRpb25IAFIWbG9jYWxPcGVyYXRvckV4Y2VwdGlvbhKjAQoZaW5pdGlhb
  Gl6ZV9vcGVyYXRvcl9sb2dpYxgVIAEoCzJHLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLklua
  XRpYWxpemVPcGVyYXRvckxvZ2ljVjJCHOI/GRIXaW5pdGlhbGl6ZU9wZXJhdG9yTG9naWNIAFIXaW5pdGlhbGl6ZU9wZXJhdG9yT
  G9naWMSkwEKFW1vZGlmeV9vcGVyYXRvcl9sb2dpYxgWIAEoCzJDLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1c
  mUud29ya2VyLk1vZGlmeU9wZXJhdG9yTG9naWNWMkIY4j8VEhNtb2RpZnlPcGVyYXRvckxvZ2ljSABSE21vZGlmeU9wZXJhdG9yT
  G9naWMScgoMcHl0aG9uX3ByaW50GBcgASgLMjsuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuU
  Hl0aG9uUHJpbnRWMkIQ4j8NEgtweXRob25QcmludEgAUgtweXRob25QcmludBKPAQoUcmVwbGF5X2N1cnJlbnRfdHVwbGUYGCABK
  AsyQi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5SZXBsYXlDdXJyZW50VHVwbGVWMkIX4j8UE
  hJyZXBsYXlDdXJyZW50VHVwbGVIAFIScmVwbGF5Q3VycmVudFR1cGxlEo4BChNldmFsdWF0ZV9leHByZXNzaW9uGBkgASgLMkIuZ
  WR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuRXZhbHVhdGVFeHByZXNzaW9uVjJCF+I/FBISZXZhb
  HVhdGVFeHByZXNzaW9uSABSEmV2YWx1YXRlRXhwcmVzc2lvbhKnAQoad29ya2VyX2V4ZWN1dGlvbl9jb21wbGV0ZWQYZSABKAsyS
  C5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJFeGVjdXRpb25Db21wbGV0ZWRWMkId4
  j8aEhh3b3JrZXJFeGVjdXRpb25Db21wbGV0ZWRIAFIYd29ya2VyRXhlY3V0aW9uQ29tcGxldGVkQg4KDHNlYWxlZF92YWx1ZUIJ4
  j8GSABYAHgBYgZwcm90bzM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}