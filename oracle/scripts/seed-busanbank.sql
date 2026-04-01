-- TB_BPM_PROCDEF seed: definitions/부산은행/*.bpmn
SET DEFINE OFF;

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/636.bpmn', '부산은행/636.bpmn', '636.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:uengine="http://uengine" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_0bfky9r" name="서비스 요청" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_0bi2gey">
    <bpmn:participant id="Participant_1pw65yb" processRef="Process_1oscmbn">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
  ') || TO_CLOB('  </bpmn:participant>
  </bpmn:collaboration>
  <bpmn:process id="Process_1oscmbn" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties>
        <uengine:json>{"definitionName":"서비스 요청","version":"7.0","shortDescription":{"text":null},"instanceNamePattern":"서비스요청_&lt;%=instance.instanceId%&gt;"}</uengine:json>
        <uengine:variable name="appResult" type="Text">
          <uengine:json>{"defaultValue":""}</uengine:json>
        </uengine:variable>
      </uengine:properties>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1ww8ww0">
      <bpmn:lane id="Lane_1imxivu" name="역할자8">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContex') || TO_CLOB('t","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0txk19f</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_11nbfhq</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_120ld28</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_1k5ww3u" name="역할자9">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
      </bpmn:lane>
      <bpmn:lane id="Lane_0pwc0bs" name="역할자10">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleR') || TO_CLOB('esolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0a9whdd</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_18vl8vh</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1kvu8br</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0vbuk2g" name="역할자11">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_1jma0mo</bpmn:flowNodeRef>
        <bpmn:flowNodeRe') || TO_CLOB('f>Gateway_0upld80</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_01rh7dw</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1guz3ck</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1omjxev</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0i91w4y</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_08dfgvx</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1gejrg9</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1q8pxmg</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0k93p5s</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0q0plkv</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0yqjbmb" name="역할자12">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.ov') || TO_CLOB('erriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0m061g4</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0jn086h</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_08rkfrm</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0wk3lff" name="역할자7">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0nu54el</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_071ax4x</bpmn:flowNodeRef>
        <bp') || TO_CLOB('mn:flowNodeRef>Activity_0w8twu8</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_05j94ov" name="역할자6">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_1jez24b</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1hfcu9v</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1lnzzor</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1ue9cwr</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_05pb2do</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0uflaxj</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_12') || TO_CLOB('czbvq</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_02y8rpe</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0nz3lgi</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1alkeve</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1407m1q</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0mo5mpb</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1grz0af</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1cmtixk</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1jfbgxu</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1detht6</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1g4yq3k</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1uzenuf</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0nokimr</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_') || TO_CLOB('1nxco1t</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_15kahj7</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_03cbfll</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1w4g4da</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0tk90nc" name="역할자5">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_1p46ujf</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0s3gln3</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_06ypmeu" name="역할자4">
        <bpmn:extensionElements>
          <uengin') || TO_CLOB('e:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0qe6s5b</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_04s6twi</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_1vbqufe" name="역할자3">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_1enp788</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_02r1r') || TO_CLOB('6u</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0u3sy4q</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0y27tcu</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_00q44mv</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_1sb5j0h" name="역할자2">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Gateway_0k7xvhh</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0t6fuaa</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0jo1vtt" name="역할자">
        <bpmn:extensionElements>
          <uengine:proper') || TO_CLOB('ties>
            <uengine:json>{"roleResolutionContext":{"_type":"org.uengine.five.overriding.IAMRoleResolutionContext","scope":"manager"}}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Event_0j0vl3w</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_1vztoge</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0lidx6r</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:exclusiveGateway id="Gateway_1enp788" default="Flow_0gqfdyz">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1jdtnuq</bpmn:incoming>
      <bpmn:outgoing>Flow_0gqfdyz</bpmn:outgoing>
      <bpmn:outgoing>') || TO_CLOB('Flow_1m27gfj</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_02r1r6u" default="Flow_1s1uzeu">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1fqegxc</bpmn:incoming>
      <bpmn:incoming>Flow_1m27gfj</bpmn:incoming>
      <bpmn:outgoing>Flow_1s1uzeu</bpmn:outgoing>
      <bpmn:outgoing>Flow_0ngw7a5</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0u3sy4q" default="Flow_0j6yc8v">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1s1uzeu</bpmn:i') || TO_CLOB('ncoming>
      <bpmn:outgoing>Flow_0j6yc8v</bpmn:outgoing>
      <bpmn:outgoing>Flow_1vp36d9</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:startEvent id="Event_0j0vl3w">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_0vkuntt</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:exclusiveGateway id="Gateway_1vztoge">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0bb75kv</bpmn:incoming>
      <bpmn:incoming>Flow_17sjbnj</bpmn:incoming>
      <bpmn:outgoing>Flow_17mly68</bpmn:outgoing>
      <bpmn:outgoing>') || TO_CLOB('Flow_1jdtnuq</bpmn:outgoing>
      <bpmn:outgoing>Flow_1byw4vr</bpmn:outgoing>
      <bpmn:outgoing>Flow_17lvqwh</bpmn:outgoing>
      <bpmn:outgoing>Flow_149r84n</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1jdtnuq" name="B" sourceRef="Gateway_1vztoge" targetRef="Gateway_1enp788">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"B","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0gqfdyz" sourceRef="Gateway_1enp788" targetRef="Activity_00q44mv" />
    <bpmn:sequenceFlow id="Flow_1m27gfj" name="Y" sourceRef="Gateway_1enp788" targetRef="Gateway_') || TO_CLOB('02r1r6u">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1fqegxc" sourceRef="Activity_00q44mv" targetRef="Gateway_02r1r6u" />
    <bpmn:sequenceFlow id="Flow_1s1uzeu" sourceRef="Gateway_02r1r6u" targetRef="Gateway_0u3sy4q" />
    <bpmn:sequenceFlow id="Flow_0ngw7a5" name="N" sourceRef="Gateway_02r1r6u" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:') || TO_CLOB('json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0j6yc8v" sourceRef="Gateway_0u3sy4q" targetRef="Gateway_0y27tcu" />
    <bpmn:sequenceFlow id="Flow_1vp36d9" name="N" sourceRef="Gateway_0u3sy4q" targetRef="Activity_0s3gln3">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_009pvef" sourceRef="Gateway_1p46ujf" targetRef="Gateway_0y27tcu" />
    <bpmn:sequenceFlow id="Flow_0e3dydp" name="Y" sourceRef="Gateway_0y27tcu" targetRef="Activity_04s6twi">
    ') || TO_CLOB('  <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0vkuntt" sourceRef="Event_0j0vl3w" targetRef="Activity_0lidx6r" />
    <bpmn:sequenceFlow id="Flow_1o5mi70" name="Y" sourceRef="Gateway_0k7xvhh" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow ') || TO_CLOB('id="Flow_06zf29n" sourceRef="Gateway_1p46ujf" targetRef="Activity_0lidx6r" />
    <bpmn:sequenceFlow id="Flow_0bb75kv" sourceRef="Activity_0lidx6r" targetRef="Gateway_1vztoge" />
    <bpmn:sequenceFlow id="Flow_17mly68" name="A" sourceRef="Gateway_1vztoge" targetRef="Activity_0t6fuaa">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"A","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0tnkjt2" sourceRef="Activity_0t6fuaa" targetRef="Gateway_0k7xvhh" />
    <bpmn:sequenceFlow id="Flow_17e0zzj" sourceRef="Activity_0s3gln3" targetRef="Gateway_1p46ujf" />
    <bpmn:exclusiveGatew') || TO_CLOB('ay id="Gateway_1p46ujf">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_17e0zzj</bpmn:incoming>
      <bpmn:outgoing>Flow_06zf29n</bpmn:outgoing>
      <bpmn:outgoing>Flow_009pvef</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0y27tcu" default="Flow_06vnita">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0j6yc8v</bpmn:incoming>
      <bpmn:incoming>Flow_009pvef</bpmn:incoming>
      <bpmn:outgoing>Flow_0e3dydp</bpmn:outgoing>
      <bpmn:outgoing>Flow_06vnita</bpmn:outgoi') || TO_CLOB('ng>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0qe6s5b" default="Flow_03cbdyq">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0ctv7w4</bpmn:incoming>
      <bpmn:outgoing>Flow_1dvl4ey</bpmn:outgoing>
      <bpmn:outgoing>Flow_03cbdyq</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0ctv7w4" sourceRef="Activity_04s6twi" targetRef="Gateway_0qe6s5b" />
    <bpmn:sequenceFlow id="Flow_1dvl4ey" name="Y" sourceRef="Gateway_0qe6s5b" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"') || TO_CLOB('appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1byw4vr" name="C" sourceRef="Gateway_1vztoge" targetRef="Activity_1detht6">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"C","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:exclusiveGateway id="Gateway_1jez24b" default="Flow_0p7nulw">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow') || TO_CLOB('_12aj5g0</bpmn:incoming>
      <bpmn:outgoing>Flow_0p7nulw</bpmn:outgoing>
      <bpmn:outgoing>Flow_1xv1ckt</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_12aj5g0" sourceRef="Activity_1detht6" targetRef="Gateway_1jez24b" />
    <bpmn:sequenceFlow id="Flow_0p7nulw" sourceRef="Gateway_1jez24b" targetRef="Activity_0w8twu8" />
    <bpmn:sequenceFlow id="Flow_1xv1ckt" name="Y" sourceRef="Gateway_1jez24b" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:exclusiveGateway id="Gateway_0nu54el" d') || TO_CLOB('efault="Flow_175y08a">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_088agr3</bpmn:incoming>
      <bpmn:outgoing>Flow_091366q</bpmn:outgoing>
      <bpmn:outgoing>Flow_175y08a</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_088agr3" sourceRef="Activity_0w8twu8" targetRef="Gateway_0nu54el" />
    <bpmn:sequenceFlow id="Flow_091366q" name="Y" sourceRef="Gateway_0nu54el" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:propert') || TO_CLOB('ies>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_175y08a" sourceRef="Gateway_0nu54el" targetRef="Gateway_071ax4x" />
    <bpmn:sequenceFlow id="Flow_03cbdyq" sourceRef="Gateway_0qe6s5b" targetRef="Gateway_071ax4x" />
    <bpmn:sequenceFlow id="Flow_06vnita" sourceRef="Gateway_0y27tcu" targetRef="Gateway_071ax4x" />
    <bpmn:exclusiveGateway id="Gateway_0k7xvhh" default="Flow_0iebvlr">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0tnkjt2</bpmn:incoming>
      <bpmn:outgoing>Flow_1o5mi70</bpmn:outgoing>
      <bpmn:outgoing>Flow_0iebvlr</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow ') || TO_CLOB('id="Flow_0iebvlr" sourceRef="Gateway_0k7xvhh" targetRef="Gateway_071ax4x" />
    <bpmn:sequenceFlow id="Flow_17lvqwh" name="D" sourceRef="Gateway_1vztoge" targetRef="Gateway_071ax4x">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"D","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_131uunn" name="Y" sourceRef="Gateway_071ax4x" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:pro') || TO_CLOB('perties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1n65f7z" sourceRef="Gateway_071ax4x" targetRef="Gateway_11nbfhq" />
    <bpmn:sequenceFlow id="Flow_16bkbtb" name="N" sourceRef="Gateway_11nbfhq" targetRef="Activity_1kvu8br">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_16p2hka" sourceRef="Gateway_11nbfhq" targetRef="Gateway_18vl8vh" />
    <bpmn:sequenceFlow id="Flow_09zxjem" name="Z" sourceRef="Gateway_0a9whdd" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
') || TO_CLOB('        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Z","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0dub0xw" name="Y" sourceRef="Gateway_18vl8vh" targetRef="Activity_120ld28">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0u2t2eb" sourceRef="Activity_120ld28" targetRef="Gateway_0txk19f" />
    <bpmn:sequenceFlow id="Flow_0haxbi8" sourceR') || TO_CLOB('ef="Gateway_0txk19f" targetRef="Activity_1g4yq3k" />
    <bpmn:exclusiveGateway id="Gateway_0txk19f" default="Flow_0haxbi8">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0u2t2eb</bpmn:incoming>
      <bpmn:outgoing>Flow_0haxbi8</bpmn:outgoing>
      <bpmn:outgoing>Flow_1rop8o5</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_1hfcu9v" default="Flow_1ph89i8">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0vtdoso</bpmn:incoming>
      <bpmn:outgoing>Flow_0l6kkv4</bpmn:outgoi') || TO_CLOB('ng>
      <bpmn:outgoing>Flow_1ph89i8</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0vtdoso" sourceRef="Activity_1g4yq3k" targetRef="Gateway_1hfcu9v" />
    <bpmn:sequenceFlow id="Flow_0l6kkv4" name="N" sourceRef="Gateway_1hfcu9v" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1ph89i8" sourceRef="Gateway_1hfcu9v" targetRef="Activity_1uzenuf" />
    <bpmn:exclusiveGateway id="Gateway_1lnzzor" default="Flow_1b97rsf">
      <bpmn:extensionElements>
        <uengine:') || TO_CLOB('properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1cp55ak</bpmn:incoming>
      <bpmn:outgoing>Flow_1b97rsf</bpmn:outgoing>
      <bpmn:outgoing>Flow_17sjbnj</bpmn:outgoing>
      <bpmn:outgoing>Flow_1pzrbdu</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1cp55ak" sourceRef="Activity_1uzenuf" targetRef="Gateway_1lnzzor" />
    <bpmn:exclusiveGateway id="Gateway_1ue9cwr" default="Flow_1pisbbc">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1b97rsf</bpmn:incoming>
      <bpmn:outgoing>Flow_1pisbbc</bpmn:outgoing>
      <bpmn:outgoi') || TO_CLOB('ng>Flow_12lpf5w</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1b97rsf" sourceRef="Gateway_1lnzzor" targetRef="Gateway_1ue9cwr" />
    <bpmn:sequenceFlow id="Flow_1pisbbc" sourceRef="Gateway_1ue9cwr" targetRef="Activity_0mo5mpb" />
    <bpmn:exclusiveGateway id="Gateway_05pb2do" default="Flow_061mrwg">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0716te9</bpmn:incoming>
      <bpmn:outgoing>Flow_1pz2ed2</bpmn:outgoing>
      <bpmn:outgoing>Flow_061mrwg</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0716te9" sourceRef="Activity_0mo5mpb" targetRef="Gateway_05pb2do" />
    <bpmn:exclusiveGat') || TO_CLOB('eway id="Gateway_0uflaxj" default="Flow_1s2mzdp">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1pz2ed2</bpmn:incoming>
      <bpmn:outgoing>Flow_1khty2f</bpmn:outgoing>
      <bpmn:outgoing>Flow_1s2mzdp</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1pz2ed2" name="Y" sourceRef="Gateway_05pb2do" targetRef="Gateway_0uflaxj">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:seq') || TO_CLOB('uenceFlow id="Flow_1khty2f" name="Y" sourceRef="Gateway_0uflaxj" targetRef="Activity_1grz0af">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:exclusiveGateway id="Gateway_12czbvq" default="Flow_1d9t2s1">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0wlhomc</bpmn:incoming>
      <bpmn:incoming>Flow_1s2mzdp</bpmn:incoming>
      <bpmn:outgoing>Flow_0wdi2cq</bpmn:outgoing>
      <bpmn:outgoing>Flow_1d9t2s') || TO_CLOB('1</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0wlhomc" sourceRef="Activity_1grz0af" targetRef="Gateway_12czbvq" />
    <bpmn:sequenceFlow id="Flow_0wdi2cq" name="Y" sourceRef="Gateway_12czbvq" targetRef="Activity_0nokimr">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0wt9lqp" sourceRef="Activity_0nokimr" targetRef="Activity_1nxco1t" />
    <bpmn:exclusiveGateway id="Gateway_02y8rpe" default="Flow_1pb83a5">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json') || TO_CLOB('>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1jy36ho</bpmn:incoming>
      <bpmn:outgoing>Flow_03jp3xr</bpmn:outgoing>
      <bpmn:outgoing>Flow_1bcerz6</bpmn:outgoing>
      <bpmn:outgoing>Flow_1pb83a5</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_1jy36ho" sourceRef="Activity_1nxco1t" targetRef="Gateway_02y8rpe" />
    <bpmn:sequenceFlow id="Flow_03jp3xr" name="P" sourceRef="Gateway_02y8rpe" targetRef="Activity_15kahj7">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"P","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequence') || TO_CLOB('Flow>
    <bpmn:exclusiveGateway id="Gateway_0nz3lgi" default="Flow_167sxex">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0iuy7qe</bpmn:incoming>
      <bpmn:outgoing>Flow_167sxex</bpmn:outgoing>
      <bpmn:outgoing>Flow_0920mls</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0iuy7qe" sourceRef="Activity_15kahj7" targetRef="Gateway_0nz3lgi" />
    <bpmn:sequenceFlow id="Flow_167sxex" sourceRef="Gateway_0nz3lgi" targetRef="Activity_1cmtixk" />
    <bpmn:exclusiveGateway id="Gateway_1alkeve" default="Flow_1guksip">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
   ') || TO_CLOB('     </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0n9v8vb</bpmn:incoming>
      <bpmn:outgoing>Flow_1guksip</bpmn:outgoing>
      <bpmn:outgoing>Flow_0nclhqx</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0n9v8vb" sourceRef="Activity_1cmtixk" targetRef="Gateway_1alkeve" />
    <bpmn:sequenceFlow id="Flow_1guksip" sourceRef="Gateway_1alkeve" targetRef="Activity_1jfbgxu" />
    <bpmn:exclusiveGateway id="Gateway_1407m1q" default="Flow_1opvw7g">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_06w5ota</bpmn:incoming>
      <bpmn:incoming>Flow_149r84n</bpmn:incoming>
      <bpmn:incoming>Flo') || TO_CLOB('w_12lpf5w</bpmn:incoming>
      <bpmn:incoming>Flow_061mrwg</bpmn:incoming>
      <bpmn:incoming>Flow_0nclhqx</bpmn:incoming>
      <bpmn:outgoing>Flow_1opvw7g</bpmn:outgoing>
      <bpmn:outgoing>Flow_0as5juw</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_06w5ota" sourceRef="Activity_1jfbgxu" targetRef="Gateway_1407m1q" />
    <bpmn:sequenceFlow id="Flow_1opvw7g" sourceRef="Gateway_1407m1q" targetRef="Activity_03cbfll" />
    <bpmn:sequenceFlow id="Flow_1jy35f0" sourceRef="Activity_03cbfll" targetRef="Activity_1w4g4da" />
    <bpmn:sequenceFlow id="Flow_0as5juw" name="P" sourceRef="Gateway_1407m1q" targetRef="Activity_1q8pxmg">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluat') || TO_CLOB('e","key":"appResult","value":"P","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0a1zekc" sourceRef="Activity_1w4g4da" targetRef="Gateway_0m061g4" />
    <bpmn:sequenceFlow id="Flow_0bri0ah" name="N" sourceRef="Gateway_0i91w4y" targetRef="Activity_1w4g4da">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0rztmzz" name="N" sourceRef="Gateway_08dfgvx" targetRef="Activity_1w4g4da">
      <bpmn:extensionElements>
        <ue') || TO_CLOB('ngine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1b4hie6" name="N" sourceRef="Gateway_1guz3ck" targetRef="Activity_1w4g4da">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_149r84n" name="P" sourceRef="Gateway_1vztoge" targetRef="Gateway_1407m1q">
      <bpmn:extensionElements>
        <uengine:prope') || TO_CLOB('rties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"P","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_17sjbnj" name="A" sourceRef="Gateway_1lnzzor" targetRef="Gateway_1vztoge">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"A","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1pzrbdu" name="N" sourceRef="Gateway_1lnzzor" targetRef="Activity_0lidx6r">
      <bpmn:extensionElements>
        <uengine:properties>
    ') || TO_CLOB('      <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_12lpf5w" name="P" sourceRef="Gateway_1ue9cwr" targetRef="Gateway_1407m1q">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"P","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_061mrwg" sourceRef="Gateway_05pb2do" targetRef="Gateway_1407m1q" />
    <bpmn:sequenceFlow id="Flow_1s2mzdp" sourceRef="Gateway_0uflaxj" targetRef="Gat') || TO_CLOB('eway_12czbvq" />
    <bpmn:sequenceFlow id="Flow_1bcerz6" name="N" sourceRef="Gateway_02y8rpe" targetRef="Activity_0nokimr">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0920mls" name="N" sourceRef="Gateway_0nz3lgi" targetRef="Activity_0nokimr">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"N","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenc') || TO_CLOB('eFlow>
    <bpmn:sequenceFlow id="Flow_1d9t2s1" sourceRef="Gateway_12czbvq" targetRef="Activity_1cmtixk" />
    <bpmn:sequenceFlow id="Flow_0nclhqx" name="P" sourceRef="Gateway_1alkeve" targetRef="Gateway_1407m1q">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"P","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1pb83a5" sourceRef="Gateway_02y8rpe" targetRef="Activity_1cmtixk" />
    <bpmn:sequenceFlow id="Flow_1rop8o5" name="Y" sourceRef="Gateway_0txk19f" targetRef="Activity_1uzenuf">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"con') || TO_CLOB('dition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_11vymdz" sourceRef="Gateway_18vl8vh" targetRef="Activity_1uzenuf" />
    <bpmn:exclusiveGateway id="Gateway_071ax4x">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_175y08a</bpmn:incoming>
      <bpmn:incoming>Flow_03cbdyq</bpmn:incoming>
      <bpmn:incoming>Flow_06vnita</bpmn:incoming>
      <bpmn:incoming>Flow_0iebvlr</bpmn:incoming>
      <bpmn:incoming>Flow_17lvqwh</bpmn:incoming>
      <bpmn:outgoing>Flow_131uunn</bpm') || TO_CLOB('n:outgoing>
      <bpmn:outgoing>Flow_1n65f7z</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_11nbfhq" default="Flow_16p2hka">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1n65f7z</bpmn:incoming>
      <bpmn:outgoing>Flow_16bkbtb</bpmn:outgoing>
      <bpmn:outgoing>Flow_16p2hka</bpmn:outgoing>
      <bpmn:outgoing>Flow_0qjgxfk</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0qjgxfk" name="Z" sourceRef="Gateway_11nbfhq" targetRef="Gateway_0upld80">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","') || TO_CLOB('key":"appResult","value":"Z","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:exclusiveGateway id="Gateway_0a9whdd" default="Flow_16hi52k">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0vk2zrf</bpmn:incoming>
      <bpmn:outgoing>Flow_16hi52k</bpmn:outgoing>
      <bpmn:outgoing>Flow_09zxjem</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_18vl8vh" default="Flow_11vymdz">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>') || TO_CLOB('
      <bpmn:incoming>Flow_16hi52k</bpmn:incoming>
      <bpmn:incoming>Flow_16p2hka</bpmn:incoming>
      <bpmn:outgoing>Flow_0dub0xw</bpmn:outgoing>
      <bpmn:outgoing>Flow_11vymdz</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0vk2zrf" sourceRef="Activity_1kvu8br" targetRef="Gateway_0a9whdd" />
    <bpmn:sequenceFlow id="Flow_16hi52k" sourceRef="Gateway_0a9whdd" targetRef="Gateway_18vl8vh" />
    <bpmn:exclusiveGateway id="Gateway_1jma0mo" default="Flow_1uav9t8">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_06rnej2</bpmn:incoming>
      <bpmn:outgoing>Flow_10c8e4m</bpmn:outgoing>
      <bpmn:outgoing>Flow_1uav9t8') || TO_CLOB('</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0upld80">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0ohnspo</bpmn:incoming>
      <bpmn:incoming>Flow_0pwmy0v</bpmn:incoming>
      <bpmn:incoming>Flow_0e3ktoi</bpmn:incoming>
      <bpmn:incoming>Flow_1uav9t8</bpmn:incoming>
      <bpmn:incoming>Flow_0qjgxfk</bpmn:incoming>
      <bpmn:outgoing>Flow_0wswyvn</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:endEvent id="Event_01rh7dw">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bp') || TO_CLOB('mn:incoming>Flow_0wswyvn</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:exclusiveGateway id="Gateway_0m061g4" default="Flow_1g77g81">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0a1zekc</bpmn:incoming>
      <bpmn:outgoing>Flow_1dpkote</bpmn:outgoing>
      <bpmn:outgoing>Flow_1g77g81</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_1guz3ck" default="Flow_09u2uj0">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0ypw3em</bpmn:incoming>
      <bpmn:outgoing>Flow_09u2uj0</bp') || TO_CLOB('mn:outgoing>
      <bpmn:outgoing>Flow_1b4hie6</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_1omjxev" default="Flow_0u2kln4">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_09u2uj0</bpmn:incoming>
      <bpmn:incoming>Flow_1g77g81</bpmn:incoming>
      <bpmn:outgoing>Flow_1kawe4h</bpmn:outgoing>
      <bpmn:outgoing>Flow_0u2kln4</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0i91w4y" default="Flow_1l53axk">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <') || TO_CLOB('bpmn:incoming>Flow_14i4jxu</bpmn:incoming>
      <bpmn:incoming>Flow_0u2kln4</bpmn:incoming>
      <bpmn:outgoing>Flow_1l53axk</bpmn:outgoing>
      <bpmn:outgoing>Flow_0bri0ah</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_0jn086h" default="Flow_0e3ktoi">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1l53axk</bpmn:incoming>
      <bpmn:outgoing>Flow_1y3zowz</bpmn:outgoing>
      <bpmn:outgoing>Flow_0e3ktoi</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:exclusiveGateway id="Gateway_08dfgvx" default="Flow_0pwmy0v">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uen') || TO_CLOB('gine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0glbdip</bpmn:incoming>
      <bpmn:outgoing>Flow_0pwmy0v</bpmn:outgoing>
      <bpmn:outgoing>Flow_0rztmzz</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_06rnej2" sourceRef="Activity_1q8pxmg" targetRef="Gateway_1jma0mo" />
    <bpmn:sequenceFlow id="Flow_10c8e4m" name="Y" sourceRef="Gateway_1jma0mo" targetRef="Activity_1gejrg9">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1uav9t8" sourceRef="') || TO_CLOB('Gateway_1jma0mo" targetRef="Gateway_0upld80" />
    <bpmn:sequenceFlow id="Flow_0ohnspo" sourceRef="Activity_1gejrg9" targetRef="Gateway_0upld80" />
    <bpmn:sequenceFlow id="Flow_0pwmy0v" sourceRef="Gateway_08dfgvx" targetRef="Gateway_0upld80" />
    <bpmn:sequenceFlow id="Flow_0e3ktoi" sourceRef="Gateway_0jn086h" targetRef="Gateway_0upld80" />
    <bpmn:sequenceFlow id="Flow_0wswyvn" sourceRef="Gateway_0upld80" targetRef="Event_01rh7dw" />
    <bpmn:sequenceFlow id="Flow_1dpkote" name="Y" sourceRef="Gateway_0m061g4" targetRef="Activity_08rkfrm">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:ex') || TO_CLOB('tensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1g77g81" sourceRef="Gateway_0m061g4" targetRef="Gateway_1omjxev" />
    <bpmn:sequenceFlow id="Flow_0ypw3em" sourceRef="Activity_08rkfrm" targetRef="Gateway_1guz3ck" />
    <bpmn:sequenceFlow id="Flow_09u2uj0" sourceRef="Gateway_1guz3ck" targetRef="Gateway_1omjxev" />
    <bpmn:sequenceFlow id="Flow_1kawe4h" name="Y" sourceRef="Gateway_1omjxev" targetRef="Activity_0k93p5s">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0u2kln4" sourceRef="Gateway_1o') || TO_CLOB('mjxev" targetRef="Gateway_0i91w4y" />
    <bpmn:sequenceFlow id="Flow_14i4jxu" sourceRef="Activity_0k93p5s" targetRef="Gateway_0i91w4y" />
    <bpmn:sequenceFlow id="Flow_1l53axk" sourceRef="Gateway_0i91w4y" targetRef="Gateway_0jn086h" />
    <bpmn:sequenceFlow id="Flow_1y3zowz" name="Y" sourceRef="Gateway_0jn086h" targetRef="Activity_0q0plkv">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"condition":{"_type":"org.uengine.kernel.Evaluate","key":"appResult","value":"Y","condition":"=="}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0glbdip" sourceRef="Activity_0q0plkv" targetRef="Gateway_08dfgvx" />
    <bpmn:callActivity id="Activity_0mo5mpb" name="콜 시작1">
      ') || TO_CLOB('<bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"variableBindings":[],"roleBindings":[],"definitionId":"636_sub.bpmn"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1pisbbc</bpmn:incoming>
      <bpmn:outgoing>Flow_0716te9</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:callActivity id="Activity_1grz0af" name="콜 시작2">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"variableBindings":[],"roleBindings":[],"definitionId":"636_sub2.bpmn"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1khty2f</bpmn:incoming>
      <bpmn:outgoing>Flow_0wlhomc</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:callActivity id="Act') || TO_CLOB('ivity_1cmtixk" name="콜 시작3">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"variableBindings":[],"roleBindings":[],"definitionId":"636_sub.bpmn"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_167sxex</bpmn:incoming>
      <bpmn:incoming>Flow_1d9t2s1</bpmn:incoming>
      <bpmn:incoming>Flow_1pb83a5</bpmn:incoming>
      <bpmn:outgoing>Flow_0n9v8vb</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:callActivity id="Activity_1jfbgxu" name="콜 시작4">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"variableBindings":[],"roleBindings":[],"definitionId":"636_sub.bpmn"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>') || TO_CLOB('Flow_1guksip</bpmn:incoming>
      <bpmn:outgoing>Flow_06w5ota</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:callActivity id="Activity_1gejrg9" name="콜 시작5">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"variableBindings":[],"roleBindings":[],"definitionId":"636_sub.bpmn"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_10c8e4m</bpmn:incoming>
      <bpmn:outgoing>Flow_0ohnspo</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:userTask id="Activity_0lidx6r" name="서비스 요청">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingCo') || TO_CLOB('ntext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0ngw7a5</bpmn:incoming>
      <bpmn:incoming>Flow_0vkuntt</bpmn:incoming>
      <bpmn:incoming>Flow_1o5mi70</bpmn:incoming>
      <bpmn:incoming>Flow_06zf29n</bpmn:incoming>
      <bpmn:incoming>Flow_1dvl4ey</bpmn:incoming>
      <bpmn:incoming>Flow_1xv1ckt</bpmn:incoming>
      <bpmn:incoming>Flow_091366q</bpmn:incoming>
      <bpmn:incoming>Flow_131uunn</bpmn:incoming>
      <bpmn:incoming>Flow_09zxjem</bpmn:incoming>
      <bpmn:incoming>Flow_0l6kkv4</bpmn:incoming>
') || TO_CLOB('      <bpmn:incoming>Flow_1pzrbdu</bpmn:incoming>
      <bpmn:outgoing>Flow_0bb75kv</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0t6fuaa" name="서비스 등록">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_17mly68</bpmn:incoming>
      <bpmn:outgoing>Flow_0tnkjt2</bpmn:outgoing>
') || TO_CLOB('    </bpmn:userTask>
    <bpmn:userTask id="Activity_00q44mv" name="서비스 시작1">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0gqfdyz</bpmn:incoming>
      <bpmn:outgoing>Flow_1fqegxc</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0s3gln3" name="서비스 시작2">
      <bpmn:extension') || TO_CLOB('Elements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1vp36d9</bpmn:incoming>
      <bpmn:outgoing>Flow_17e0zzj</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_04s6twi" name="서비스 시작5">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType"') || TO_CLOB(':"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0e3dydp</bpmn:incoming>
      <bpmn:outgoing>Flow_0ctv7w4</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1detht6" name="서비스 시작3">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappi') || TO_CLOB('ngContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1byw4vr</bpmn:incoming>
      <bpmn:outgoing>Flow_12aj5g0</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0w8twu8" name="서비스 시작4">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name') || TO_CLOB('":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0p7nulw</bpmn:incoming>
      <bpmn:outgoing>Flow_088agr3</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1kvu8br" name="서비스 시작 6">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.u') || TO_CLOB('engine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_16bkbtb</bpmn:incoming>
      <bpmn:outgoing>Flow_0vk2zrf</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_120ld28" name="서비스 시작7">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionEl') || TO_CLOB('ements>
      <bpmn:incoming>Flow_0dub0xw</bpmn:incoming>
      <bpmn:outgoing>Flow_0u2t2eb</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1g4yq3k" name="서비스 시작8">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0haxbi8</bpmn:incoming>
      <bpmn:outgoing>Flow_0vtdoso</bpmn:o') || TO_CLOB('utgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1uzenuf" name="서비스 시작9">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ph89i8</bpmn:incoming>
      <bpmn:incoming>Flow_1rop8o5</bpmn:incoming>
      <bpmn:incoming>Flow_11vymdz</bpmn:incoming>
      <bpmn:outgoing>Flow_1cp55ak</bpmn:') || TO_CLOB('outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0nokimr" name="서비스 시작10">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0wdi2cq</bpmn:incoming>
      <bpmn:incoming>Flow_1bcerz6</bpmn:incoming>
      <bpmn:incoming>Flow_0920mls</bpmn:incoming>
      <bpmn:outgoing>Flow_0wt9lqp</bpm') || TO_CLOB('n:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1nxco1t" name="서비스 시작11">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0wt9lqp</bpmn:incoming>
      <bpmn:outgoing>Flow_1jy36ho</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_15kahj7" name="서비스 시작12">
      <') || TO_CLOB('bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_03jp3xr</bpmn:incoming>
      <bpmn:outgoing>Flow_0iuy7qe</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_03cbfll" name="서비스 시작13">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronizatio') || TO_CLOB('n":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1opvw7g</bpmn:incoming>
      <bpmn:outgoing>Flow_1jy35f0</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1w4g4da" name="서비스 시작14">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey') || TO_CLOB('":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1jy35f0</bpmn:incoming>
      <bpmn:incoming>Flow_0bri0ah</bpmn:incoming>
      <bpmn:incoming>Flow_0rztmzz</bpmn:incoming>
      <bpmn:incoming>Flow_1b4hie6</bpmn:incoming>
      <bpmn:outgoing>Flow_0a1zekc</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1q8pxmg" name="서비스 시작 15">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"app') || TO_CLOB('Result","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0as5juw</bpmn:incoming>
      <bpmn:outgoing>Flow_06rnej2</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_08rkfrm" name="서비스 시작 16">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements') || TO_CLOB('":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1dpkote</bpmn:incoming>
      <bpmn:outgoing>Flow_0ypw3em</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0k93p5s" name="서비스 시작17">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","') || TO_CLOB('askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1kawe4h</bpmn:incoming>
      <bpmn:outgoing>Flow_14i4jxu</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0q0plkv" name="서비스 시작18">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"eventSynchronization":{"eventType":"","attributes":[{"name":"appResult","className":"String","isKey":false,"isCorrKey":false}],"mappingContext":{"mappingElements":[{"argument":{"text":"appResult"},"direction":"out","variable":{"name":"[Arguments].appResult","askWhenInit":false,"isVolatile":false},"isKey":false}]}},"_type":"org.uengine.kernel.HumanActivity') || TO_CLOB('"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1y3zowz</bpmn:incoming>
      <bpmn:outgoing>Flow_0glbdip</bpmn:outgoing>
    </bpmn:userTask>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_0bi2gey">
      <bpmndi:BPMNShape id="Participant_1pw65yb_di" bpmnElement="Participant_1pw65yb" isHorizontal="true">
        <dc:Bounds x="110" y="-2440" width="7230" height="3115" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0jo1vtt_di" bpmnElement="Lane_0jo1vtt" isHorizontal="true">
        <dc:Bounds x="140" y="485" width="7200" height="190" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1sb5j0h_di" bpmnEl') || TO_CLOB('ement="Lane_1sb5j0h" isHorizontal="true">
        <dc:Bounds x="140" y="255" width="7200" height="230" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1vbqufe_di" bpmnElement="Lane_1vbqufe" isHorizontal="true">
        <dc:Bounds x="140" y="45" width="7200" height="210" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_06ypmeu_di" bpmnElement="Lane_06ypmeu" isHorizontal="true">
        <dc:Bounds x="140" y="-135" width="7200" height="180" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0tk90nc_di" bpmnElement="Lane_0tk90nc" isHorizontal="true">
        <dc:Bounds x="140" y="-410" width="7200" height="275" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
  ') || TO_CLOB('    <bpmndi:BPMNShape id="Lane_05j94ov_di" bpmnElement="Lane_05j94ov" isHorizontal="true">
        <dc:Bounds x="140" y="-700" width="7200" height="290" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0wk3lff_di" bpmnElement="Lane_0wk3lff" isHorizontal="true">
        <dc:Bounds x="140" y="-1020" width="7200" height="320" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0yqjbmb_di" bpmnElement="Lane_0yqjbmb" isHorizontal="true">
        <dc:Bounds x="140" y="-2440" width="7200" height="360" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0vbuk2g_di" bpmnElement="Lane_0vbuk2g" isHorizontal="true">
        <dc:Bounds x="140" y="-2080" width="7200" height="240" />
  ') || TO_CLOB('      <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0pwc0bs_di" bpmnElement="Lane_0pwc0bs" isHorizontal="true">
        <dc:Bounds x="140" y="-1840" width="7200" height="370" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1k5ww3u_di" bpmnElement="Lane_1k5ww3u" isHorizontal="true">
        <dc:Bounds x="140" y="-1470" width="7200" height="160" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1imxivu_di" bpmnElement="Lane_1imxivu" isHorizontal="true">
        <dc:Bounds x="140" y="-1310" width="7200" height="290" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1enp788_di" bpmnElement="Gateway_1enp788" isMarkerVisible="true">
   ') || TO_CLOB('     <dc:Bounds x="525" y="130" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_02r1r6u_di" bpmnElement="Gateway_02r1r6u" isMarkerVisible="true">
        <dc:Bounds x="805" y="130" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0u3sy4q_di" bpmnElement="Gateway_0u3sy4q" isMarkerVisible="true">
        <dc:Bounds x="925" y="130" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0j0vl3w_di" bpmnElement="Event_0j0vl3w">
        <dc:Bounds x="172" y="557" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1vztoge_di" bpmnElement="Gateway_1vztoge" isMarkerVisible="true">
        <dc:Bounds x="415" y="550" width="50" height="50" />
      </bpmnd') || TO_CLOB('i:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1p46ujf_di" bpmnElement="Gateway_1p46ujf" isMarkerVisible="true">
        <dc:Bounds x="1055" y="-305" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0y27tcu_di" bpmnElement="Gateway_0y27tcu" isMarkerVisible="true">
        <dc:Bounds x="1055" y="130" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0qe6s5b_di" bpmnElement="Gateway_0qe6s5b" isMarkerVisible="true">
        <dc:Bounds x="1455" y="-75" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1jez24b_di" bpmnElement="Gateway_1jez24b" isMarkerVisible="true">
        <dc:Bounds x="655" y="-525" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id') || TO_CLOB('="Gateway_0nu54el_di" bpmnElement="Gateway_0nu54el" isMarkerVisible="true">
        <dc:Bounds x="1235" y="-865" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0k7xvhh_di" bpmnElement="Gateway_0k7xvhh" isMarkerVisible="true">
        <dc:Bounds x="665" y="360" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0txk19f_di" bpmnElement="Gateway_0txk19f" isMarkerVisible="true">
        <dc:Bounds x="2685" y="-1135" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1hfcu9v_di" bpmnElement="Gateway_1hfcu9v" isMarkerVisible="true">
        <dc:Bounds x="2815" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1lnzzor_di" bpmnElement="Ga') || TO_CLOB('teway_1lnzzor" isMarkerVisible="true">
        <dc:Bounds x="3075" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1ue9cwr_di" bpmnElement="Gateway_1ue9cwr" isMarkerVisible="true">
        <dc:Bounds x="3185" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_05pb2do_di" bpmnElement="Gateway_05pb2do" isMarkerVisible="true">
        <dc:Bounds x="3465" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0uflaxj_di" bpmnElement="Gateway_0uflaxj" isMarkerVisible="true">
        <dc:Bounds x="3585" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_12czbvq_di" bpmnElement="Gateway_12czbvq" isMarkerVisible="true') || TO_CLOB('">
        <dc:Bounds x="3885" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_02y8rpe_di" bpmnElement="Gateway_02y8rpe" isMarkerVisible="true">
        <dc:Bounds x="4375" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0nz3lgi_di" bpmnElement="Gateway_0nz3lgi" isMarkerVisible="true">
        <dc:Bounds x="4695" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1alkeve_di" bpmnElement="Gateway_1alkeve" isMarkerVisible="true">
        <dc:Bounds x="5015" y="-605" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1407m1q_di" bpmnElement="Gateway_1407m1q" isMarkerVisible="true">
        <dc:Bounds x="5335" y="-6') || TO_CLOB('05" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_071ax4x_di" bpmnElement="Gateway_071ax4x" isMarkerVisible="true">
        <dc:Bounds x="1645" y="-865" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_11nbfhq_di" bpmnElement="Gateway_11nbfhq" isMarkerVisible="true">
        <dc:Bounds x="1925" y="-1205" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0a9whdd_di" bpmnElement="Gateway_0a9whdd" isMarkerVisible="true">
        <dc:Bounds x="2315" y="-1645" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_18vl8vh_di" bpmnElement="Gateway_18vl8vh" isMarkerVisible="true">
        <dc:Bounds x="2485" y="-1645" width="50" height="50" />
   ') || TO_CLOB('   </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1jma0mo_di" bpmnElement="Gateway_1jma0mo" isMarkerVisible="true">
        <dc:Bounds x="5465" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0upld80_di" bpmnElement="Gateway_0upld80" isMarkerVisible="true">
        <dc:Bounds x="7135" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_01rh7dw_di" bpmnElement="Event_01rh7dw">
        <dc:Bounds x="7252" y="-1968" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0m061g4_di" bpmnElement="Gateway_0m061g4" isMarkerVisible="true">
        <dc:Bounds x="5785" y="-2195" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_') || TO_CLOB('1guz3ck_di" bpmnElement="Gateway_1guz3ck" isMarkerVisible="true">
        <dc:Bounds x="6065" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_1omjxev_di" bpmnElement="Gateway_1omjxev" isMarkerVisible="true">
        <dc:Bounds x="6195" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0i91w4y_di" bpmnElement="Gateway_0i91w4y" isMarkerVisible="true">
        <dc:Bounds x="6515" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0jn086h_di" bpmnElement="Gateway_0jn086h" isMarkerVisible="true">
        <dc:Bounds x="6515" y="-2195" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_08dfgvx_di" bpmnElement="Gateway') || TO_CLOB('_08dfgvx" isMarkerVisible="true">
        <dc:Bounds x="6885" y="-1975" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0175w6e_di" bpmnElement="Activity_0mo5mpb">
        <dc:Bounds x="3300" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1656m47_di" bpmnElement="Activity_1grz0af">
        <dc:Bounds x="3710" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_05pa07v_di" bpmnElement="Activity_1cmtixk">
        <dc:Bounds x="4830" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1nha3ix_di" bpmnElement="Activity_1jfbg') || TO_CLOB('xu">
        <dc:Bounds x="5150" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0wvneuu_di" bpmnElement="Activity_1gejrg9">
        <dc:Bounds x="5570" y="-1990" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_09vbl93_di" bpmnElement="Activity_0lidx6r">
        <dc:Bounds x="260" y="535" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_02vfzig_di" bpmnElement="Activity_0t6fuaa">
        <dc:Bounds x="500" y="345" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_08b0xrt_di" bpmnElement="Activity_00q44mv"') || TO_CLOB('>
        <dc:Bounds x="640" y="115" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0dw1fzm_di" bpmnElement="Activity_0s3gln3">
        <dc:Bounds x="900" y="-320" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_08q5r42_di" bpmnElement="Activity_04s6twi">
        <dc:Bounds x="1230" y="-90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0oeior4_di" bpmnElement="Activity_1detht6">
        <dc:Bounds x="510" y="-540" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1aejcvr_di" bpmnElement="Activity_0w8twu8">
   ') || TO_CLOB('     <dc:Bounds x="920" y="-880" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1gvtajy_di" bpmnElement="Activity_1kvu8br">
        <dc:Bounds x="2100" y="-1660" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_00zbbxr_di" bpmnElement="Activity_120ld28">
        <dc:Bounds x="2660" y="-1280" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0n41c7j_di" bpmnElement="Activity_1g4yq3k">
        <dc:Bounds x="2660" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0zesp2a_di" bpmnElement="Activity_1uzenuf">
  ') || TO_CLOB('      <dc:Bounds x="2920" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_12yhcjq_di" bpmnElement="Activity_0nokimr">
        <dc:Bounds x="4010" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1orrz95_di" bpmnElement="Activity_1nxco1t">
        <dc:Bounds x="4190" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1df74cg_di" bpmnElement="Activity_15kahj7">
        <dc:Bounds x="4510" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0q3jwv1_di" bpmnElement="Activity_03cbfll">
  ') || TO_CLOB('      <dc:Bounds x="5530" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_004ur9o_di" bpmnElement="Activity_1w4g4da">
        <dc:Bounds x="5760" y="-620" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_168hbt3_di" bpmnElement="Activity_1q8pxmg">
        <dc:Bounds x="5310" y="-1990" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0uqjo5g_di" bpmnElement="Activity_08rkfrm">
        <dc:Bounds x="5890" y="-2210" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_17ltojm_di" bpmnElement="Activity_0k93p5s">
') || TO_CLOB('        <dc:Bounds x="6290" y="-1990" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0w5pwoi_di" bpmnElement="Activity_0q0plkv">
        <dc:Bounds x="6680" y="-1990" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1jdtnuq_di" bpmnElement="Flow_1jdtnuq">
        <di:waypoint x="440" y="550" />
        <di:waypoint x="440" y="155" />
        <di:waypoint x="525" y="155" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="451" y="173" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0gqfdyz_di" bpmnElement="Flow_0gqfdyz">
        <di:waypoint x="575" y="155" />
        <di:waypoint x="640" y="1') || TO_CLOB('55" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1m27gfj_di" bpmnElement="Flow_1m27gfj">
        <di:waypoint x="550" y="180" />
        <di:waypoint x="550" y="225" />
        <di:waypoint x="830" y="225" />
        <di:waypoint x="830" y="180" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="686" y="207" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1fqegxc_di" bpmnElement="Flow_1fqegxc">
        <di:waypoint x="740" y="155" />
        <di:waypoint x="805" y="155" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1s1uzeu_di" bpmnElement="Flow_1s1uzeu">
        <di:waypoint x="855" y="155" />
        <di:waypoint x="925" y="155" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0ngw7a5_') || TO_CLOB('di" bpmnElement="Flow_0ngw7a5">
        <di:waypoint x="830" y="130" />
        <di:waypoint x="830" y="85" />
        <di:waypoint x="310" y="85" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="566" y="67" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0j6yc8v_di" bpmnElement="Flow_0j6yc8v">
        <di:waypoint x="975" y="155" />
        <di:waypoint x="1055" y="155" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1vp36d9_di" bpmnElement="Flow_1vp36d9">
        <di:waypoint x="950" y="130" />
        <di:waypoint x="950" y="-240" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="961" y="-58" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEd') || TO_CLOB('ge>
      <bpmndi:BPMNEdge id="Flow_009pvef_di" bpmnElement="Flow_009pvef">
        <di:waypoint x="1080" y="-255" />
        <di:waypoint x="1080" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0e3dydp_di" bpmnElement="Flow_0e3dydp">
        <di:waypoint x="1105" y="155" />
        <di:waypoint x="1280" y="155" />
        <di:waypoint x="1280" y="-10" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1189" y="137" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0vkuntt_di" bpmnElement="Flow_0vkuntt">
        <di:waypoint x="208" y="575" />
        <di:waypoint x="260" y="575" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1o5mi70_di" bpmnElement="Flow_1o5mi70">
        <di:waypoint x="690" ') || TO_CLOB('y="360" />
        <di:waypoint x="690" y="305" />
        <di:waypoint x="310" y="305" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="496" y="287" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_06zf29n_di" bpmnElement="Flow_06zf29n">
        <di:waypoint x="1080" y="-305" />
        <di:waypoint x="1080" y="-350" />
        <di:waypoint x="310" y="-350" />
        <di:waypoint x="310" y="535" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0bb75kv_di" bpmnElement="Flow_0bb75kv">
        <di:waypoint x="360" y="575" />
        <di:waypoint x="415" y="575" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_17mly68_di" bpmnElement="Flow_17mly68">
        <di:wayp') || TO_CLOB('oint x="440" y="550" />
        <di:waypoint x="440" y="385" />
        <di:waypoint x="500" y="385" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="451" y="403" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0tnkjt2_di" bpmnElement="Flow_0tnkjt2">
        <di:waypoint x="600" y="385" />
        <di:waypoint x="665" y="385" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_17e0zzj_di" bpmnElement="Flow_17e0zzj">
        <di:waypoint x="1000" y="-280" />
        <di:waypoint x="1055" y="-280" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0ctv7w4_di" bpmnElement="Flow_0ctv7w4">
        <di:waypoint x="1330" y="-50" />
        <di:waypoint x="1455" y="-50" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPM') || TO_CLOB('NEdge id="Flow_1dvl4ey_di" bpmnElement="Flow_1dvl4ey">
        <di:waypoint x="1480" y="-75" />
        <di:waypoint x="1480" y="-110" />
        <di:waypoint x="310" y="-110" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="891" y="-128" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1byw4vr_di" bpmnElement="Flow_1byw4vr">
        <di:waypoint x="440" y="550" />
        <di:waypoint x="440" y="-500" />
        <di:waypoint x="510" y="-500" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="451" y="-477" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_12aj5g0_di" bpmnElement="Flow_12aj5g0">
        <di:waypoint x="610" ') || TO_CLOB('y="-500" />
        <di:waypoint x="655" y="-500" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0p7nulw_di" bpmnElement="Flow_0p7nulw">
        <di:waypoint x="705" y="-500" />
        <di:waypoint x="970" y="-500" />
        <di:waypoint x="970" y="-800" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1xv1ckt_di" bpmnElement="Flow_1xv1ckt">
        <di:waypoint x="680" y="-525" />
        <di:waypoint x="680" y="-580" />
        <di:waypoint x="310" y="-580" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="491" y="-598" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_088agr3_di" bpmnElement="Flow_088agr3">
        <di:waypoint x="1020" y="-840" />
        <d') || TO_CLOB('i:waypoint x="1235" y="-840" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_091366q_di" bpmnElement="Flow_091366q">
        <di:waypoint x="1260" y="-865" />
        <di:waypoint x="1260" y="-940" />
        <di:waypoint x="310" y="-940" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="781" y="-958" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_175y08a_di" bpmnElement="Flow_175y08a">
        <di:waypoint x="1285" y="-840" />
        <di:waypoint x="1645" y="-840" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_03cbdyq_di" bpmnElement="Flow_03cbdyq">
        <di:waypoint x="1505" y="-50" />
        <di:waypoint x="1670" y="-50" />
        <di:waypoint x="1670') || TO_CLOB('" y="-815" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_06vnita_di" bpmnElement="Flow_06vnita">
        <di:waypoint x="1080" y="180" />
        <di:waypoint x="1080" y="210" />
        <di:waypoint x="1670" y="210" />
        <di:waypoint x="1670" y="-815" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0iebvlr_di" bpmnElement="Flow_0iebvlr">
        <di:waypoint x="715" y="385" />
        <di:waypoint x="1670" y="385" />
        <di:waypoint x="1670" y="-815" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_17lvqwh_di" bpmnElement="Flow_17lvqwh">
        <di:waypoint x="440" y="550" />
        <di:waypoint x="440" y="520" />
        <di:waypoint x="1670" y="520" />
        <di:waypoint x="1670" y="-815" />
        <bpmndi:BPMNLabel>
          <dc:Bou') || TO_CLOB('nds x="516" y="502" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_131uunn_di" bpmnElement="Flow_131uunn">
        <di:waypoint x="1670" y="-865" />
        <di:waypoint x="1670" y="-1000" />
        <di:waypoint x="310" y="-1000" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="986" y="-1018" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1n65f7z_di" bpmnElement="Flow_1n65f7z">
        <di:waypoint x="1695" y="-840" />
        <di:waypoint x="1950" y="-840" />
        <di:waypoint x="1950" y="-1155" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_16bkbtb_di" bpmnElement="Flow_16bkbtb">
        <di:waypoint x=') || TO_CLOB('"1950" y="-1205" />
        <di:waypoint x="1950" y="-1620" />
        <di:waypoint x="2100" y="-1620" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1961" y="-1415" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_16p2hka_di" bpmnElement="Flow_16p2hka">
        <di:waypoint x="1975" y="-1180" />
        <di:waypoint x="2510" y="-1180" />
        <di:waypoint x="2510" y="-1595" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_09zxjem_di" bpmnElement="Flow_09zxjem">
        <di:waypoint x="2340" y="-1645" />
        <di:waypoint x="2340" y="-1740" />
        <di:waypoint x="310" y="-1740" />
        <di:waypoint x="310" y="535" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1322" y="-1758" width="7" height') || TO_CLOB('="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0dub0xw_di" bpmnElement="Flow_0dub0xw">
        <di:waypoint x="2535" y="-1620" />
        <di:waypoint x="2710" y="-1620" />
        <di:waypoint x="2710" y="-1280" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="2619" y="-1638" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0u2t2eb_di" bpmnElement="Flow_0u2t2eb">
        <di:waypoint x="2710" y="-1200" />
        <di:waypoint x="2710" y="-1135" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0haxbi8_di" bpmnElement="Flow_0haxbi8">
        <di:waypoint x="2710" y="-1085" />
        <di:waypoint x="2710" y="-620" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="') || TO_CLOB('Flow_0vtdoso_di" bpmnElement="Flow_0vtdoso">
        <di:waypoint x="2760" y="-580" />
        <di:waypoint x="2815" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0l6kkv4_di" bpmnElement="Flow_0l6kkv4">
        <di:waypoint x="2840" y="-555" />
        <di:waypoint x="2840" y="730" />
        <di:waypoint x="310" y="730" />
        <di:waypoint x="310" y="615" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1571" y="712" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ph89i8_di" bpmnElement="Flow_1ph89i8">
        <di:waypoint x="2865" y="-580" />
        <di:waypoint x="2920" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1cp55ak_di" bpmnElement="Flow_1cp55ak">
        <di:way') || TO_CLOB('point x="3020" y="-580" />
        <di:waypoint x="3075" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1b97rsf_di" bpmnElement="Flow_1b97rsf">
        <di:waypoint x="3125" y="-580" />
        <di:waypoint x="3185" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1pisbbc_di" bpmnElement="Flow_1pisbbc">
        <di:waypoint x="3235" y="-580" />
        <di:waypoint x="3300" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0716te9_di" bpmnElement="Flow_0716te9">
        <di:waypoint x="3400" y="-580" />
        <di:waypoint x="3465" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1pz2ed2_di" bpmnElement="Flow_1pz2ed2">
        <di:waypoint x="3515" y="-580" />
        <di:waypoint x="3585" y="-580" />
       ') || TO_CLOB(' <bpmndi:BPMNLabel>
          <dc:Bounds x="3546" y="-598" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1khty2f_di" bpmnElement="Flow_1khty2f">
        <di:waypoint x="3635" y="-580" />
        <di:waypoint x="3710" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="3669" y="-598" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0wlhomc_di" bpmnElement="Flow_0wlhomc">
        <di:waypoint x="3810" y="-580" />
        <di:waypoint x="3885" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0wdi2cq_di" bpmnElement="Flow_0wdi2cq">
        <di:waypoint x="3935" y="-580" />
        <di:waypoint x="4010" y="-580" />
        <bpmndi:BPMNLabel>') || TO_CLOB('
          <dc:Bounds x="3969" y="-598" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0wt9lqp_di" bpmnElement="Flow_0wt9lqp">
        <di:waypoint x="4110" y="-580" />
        <di:waypoint x="4190" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1jy36ho_di" bpmnElement="Flow_1jy36ho">
        <di:waypoint x="4290" y="-580" />
        <di:waypoint x="4375" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_03jp3xr_di" bpmnElement="Flow_03jp3xr">
        <di:waypoint x="4425" y="-580" />
        <di:waypoint x="4510" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="4464" y="-598" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEd') || TO_CLOB('ge id="Flow_0iuy7qe_di" bpmnElement="Flow_0iuy7qe">
        <di:waypoint x="4610" y="-580" />
        <di:waypoint x="4695" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_167sxex_di" bpmnElement="Flow_167sxex">
        <di:waypoint x="4745" y="-580" />
        <di:waypoint x="4830" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0n9v8vb_di" bpmnElement="Flow_0n9v8vb">
        <di:waypoint x="4930" y="-580" />
        <di:waypoint x="5015" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1guksip_di" bpmnElement="Flow_1guksip">
        <di:waypoint x="5065" y="-580" />
        <di:waypoint x="5150" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_06w5ota_di" bpmnElement="Flow_06w5ota">
        <di:waypoint x="') || TO_CLOB('5250" y="-580" />
        <di:waypoint x="5335" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1opvw7g_di" bpmnElement="Flow_1opvw7g">
        <di:waypoint x="5385" y="-580" />
        <di:waypoint x="5530" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1jy35f0_di" bpmnElement="Flow_1jy35f0">
        <di:waypoint x="5630" y="-580" />
        <di:waypoint x="5760" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0as5juw_di" bpmnElement="Flow_0as5juw">
        <di:waypoint x="5360" y="-605" />
        <di:waypoint x="5360" y="-1910" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="5371" y="-1260" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0a1zekc_di" bpmnEle') || TO_CLOB('ment="Flow_0a1zekc">
        <di:waypoint x="5810" y="-620" />
        <di:waypoint x="5810" y="-2145" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0bri0ah_di" bpmnElement="Flow_0bri0ah">
        <di:waypoint x="6540" y="-1925" />
        <di:waypoint x="6540" y="-580" />
        <di:waypoint x="5860" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="6551" y="-1255" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0rztmzz_di" bpmnElement="Flow_0rztmzz">
        <di:waypoint x="6910" y="-1925" />
        <di:waypoint x="6910" y="-580" />
        <di:waypoint x="5860" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="6921" y="-1255" width="8" height="14" />
        </bpmndi:BPMNLabel>
      ') || TO_CLOB('</bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1b4hie6_di" bpmnElement="Flow_1b4hie6">
        <di:waypoint x="6090" y="-1925" />
        <di:waypoint x="6090" y="-580" />
        <di:waypoint x="5860" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="6101" y="-1255" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_149r84n_di" bpmnElement="Flow_149r84n">
        <di:waypoint x="440" y="600" />
        <di:waypoint x="440" y="640" />
        <di:waypoint x="5360" y="640" />
        <di:waypoint x="5360" y="-555" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="451" y="613" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_17sjbnj_di" bpmnElement="Flo') || TO_CLOB('w_17sjbnj">
        <di:waypoint x="3098" y="-557" />
        <di:waypoint x="2990" y="575" />
        <di:waypoint x="465" y="575" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="3076" y="-477" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1pzrbdu_di" bpmnElement="Flow_1pzrbdu">
        <di:waypoint x="3100" y="-555" />
        <di:waypoint x="3100" y="760" />
        <di:waypoint x="310" y="760" />
        <di:waypoint x="310" y="615" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="3116" y="-477" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_12lpf5w_di" bpmnElement="Flow_12lpf5w">
        <di:waypoint x="3210" y="-605" />
        <di:waypoint x="3210') || TO_CLOB('" y="-670" />
        <di:waypoint x="5310" y="-670" />
        <di:waypoint x="5310" y="-580" />
        <di:waypoint x="5335" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="4256" y="-688" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_061mrwg_di" bpmnElement="Flow_061mrwg">
        <di:waypoint x="3490" y="-605" />
        <di:waypoint x="3490" y="-670" />
        <di:waypoint x="5310" y="-670" />
        <di:waypoint x="5310" y="-580" />
        <di:waypoint x="5335" y="-580" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1s2mzdp_di" bpmnElement="Flow_1s2mzdp">
        <di:waypoint x="3610" y="-555" />
        <di:waypoint x="3610" y="-470" />
        <di:waypoint x="3910" y="-470" />
        <di') || TO_CLOB(':waypoint x="3910" y="-555" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1bcerz6_di" bpmnElement="Flow_1bcerz6">
        <di:waypoint x="4400" y="-555" />
        <di:waypoint x="4400" y="-440" />
        <di:waypoint x="4060" y="-440" />
        <di:waypoint x="4060" y="-540" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="4226" y="-458" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0920mls_di" bpmnElement="Flow_0920mls">
        <di:waypoint x="4720" y="-555" />
        <di:waypoint x="4720" y="-440" />
        <di:waypoint x="4060" y="-440" />
        <di:waypoint x="4060" y="-540" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="4386" y="-458" width="8" height="14" />
        </bpmndi:BPMNLabel>
 ') || TO_CLOB('     </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1d9t2s1_di" bpmnElement="Flow_1d9t2s1">
        <di:waypoint x="3910" y="-605" />
        <di:waypoint x="3910" y="-650" />
        <di:waypoint x="4880" y="-650" />
        <di:waypoint x="4880" y="-620" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0nclhqx_di" bpmnElement="Flow_0nclhqx">
        <di:waypoint x="5040" y="-605" />
        <di:waypoint x="5040" y="-670" />
        <di:waypoint x="5310" y="-670" />
        <di:waypoint x="5310" y="-580" />
        <di:waypoint x="5335" y="-580" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="5171" y="-688" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1pb83a5_di" bpmnElement="Flow_1pb83a5">
        <di:w') || TO_CLOB('aypoint x="4400" y="-605" />
        <di:waypoint x="4400" y="-650" />
        <di:waypoint x="4880" y="-650" />
        <di:waypoint x="4880" y="-620" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1rop8o5_di" bpmnElement="Flow_1rop8o5">
        <di:waypoint x="2735" y="-1110" />
        <di:waypoint x="2970" y="-1110" />
        <di:waypoint x="2970" y="-620" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="2849" y="-1128" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_11vymdz_di" bpmnElement="Flow_11vymdz">
        <di:waypoint x="2510" y="-1645" />
        <di:waypoint x="2510" y="-1740" />
        <di:waypoint x="2970" y="-1740" />
        <di:waypoint x="2970" y="-620" />
      </bpmndi:BPMNEdge>
      <') || TO_CLOB('bpmndi:BPMNEdge id="Flow_0qjgxfk_di" bpmnElement="Flow_0qjgxfk">
        <di:waypoint x="1925" y="-1180" />
        <di:waypoint x="1780" y="-1180" />
        <di:waypoint x="1780" y="-2350" />
        <di:waypoint x="7160" y="-2350" />
        <di:waypoint x="7160" y="-1975" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1792" y="-1768" width="7" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0vk2zrf_di" bpmnElement="Flow_0vk2zrf">
        <di:waypoint x="2200" y="-1620" />
        <di:waypoint x="2315" y="-1620" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_16hi52k_di" bpmnElement="Flow_16hi52k">
        <di:waypoint x="2365" y="-1620" />
        <di:waypoint x="2485" y="-1620" />
      </bpmndi:BPMNEdge>
      <bp') || TO_CLOB('mndi:BPMNEdge id="Flow_06rnej2_di" bpmnElement="Flow_06rnej2">
        <di:waypoint x="5410" y="-1950" />
        <di:waypoint x="5465" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_10c8e4m_di" bpmnElement="Flow_10c8e4m">
        <di:waypoint x="5515" y="-1950" />
        <di:waypoint x="5570" y="-1950" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="5539" y="-1968" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1uav9t8_di" bpmnElement="Flow_1uav9t8">
        <di:waypoint x="5490" y="-1975" />
        <di:waypoint x="5490" y="-2280" />
        <di:waypoint x="7160" y="-2280" />
        <di:waypoint x="7160" y="-1975" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0ohnspo_di" bpmnElemen') || TO_CLOB('t="Flow_0ohnspo">
        <di:waypoint x="5620" y="-1990" />
        <di:waypoint x="5620" y="-2280" />
        <di:waypoint x="7160" y="-2280" />
        <di:waypoint x="7160" y="-1975" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0pwmy0v_di" bpmnElement="Flow_0pwmy0v">
        <di:waypoint x="6935" y="-1950" />
        <di:waypoint x="7135" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0e3ktoi_di" bpmnElement="Flow_0e3ktoi">
        <di:waypoint x="6540" y="-2195" />
        <di:waypoint x="6540" y="-2230" />
        <di:waypoint x="7160" y="-2230" />
        <di:waypoint x="7160" y="-1975" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0wswyvn_di" bpmnElement="Flow_0wswyvn">
        <di:waypoint x="7185" y="-1950" />
        <di:waypoi') || TO_CLOB('nt x="7252" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1dpkote_di" bpmnElement="Flow_1dpkote">
        <di:waypoint x="5835" y="-2170" />
        <di:waypoint x="5890" y="-2170" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="5859" y="-2188" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1g77g81_di" bpmnElement="Flow_1g77g81">
        <di:waypoint x="5810" y="-2195" />
        <di:waypoint x="5810" y="-2240" />
        <di:waypoint x="6160" y="-2240" />
        <di:waypoint x="6160" y="-1950" />
        <di:waypoint x="6195" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0ypw3em_di" bpmnElement="Flow_0ypw3em">
        <di:waypoint x="5990" y="-2170" />
        <di:waypoint') || TO_CLOB(' x="6090" y="-2170" />
        <di:waypoint x="6090" y="-1975" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_09u2uj0_di" bpmnElement="Flow_09u2uj0">
        <di:waypoint x="6115" y="-1950" />
        <di:waypoint x="6195" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1kawe4h_di" bpmnElement="Flow_1kawe4h">
        <di:waypoint x="6245" y="-1950" />
        <di:waypoint x="6290" y="-1950" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="6264" y="-1968" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0u2kln4_di" bpmnElement="Flow_0u2kln4">
        <di:waypoint x="6220" y="-1975" />
        <di:waypoint x="6220" y="-2060" />
        <di:waypoint x="6470" y="-2060" />
        <di:waypoint x') || TO_CLOB('="6470" y="-1950" />
        <di:waypoint x="6515" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_14i4jxu_di" bpmnElement="Flow_14i4jxu">
        <di:waypoint x="6390" y="-1950" />
        <di:waypoint x="6515" y="-1950" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1l53axk_di" bpmnElement="Flow_1l53axk">
        <di:waypoint x="6540" y="-1975" />
        <di:waypoint x="6540" y="-2145" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1y3zowz_di" bpmnElement="Flow_1y3zowz">
        <di:waypoint x="6565" y="-2170" />
        <di:waypoint x="6730" y="-2170" />
        <di:waypoint x="6730" y="-1990" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="6644" y="-2188" width="8" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>') || TO_CLOB('
      <bpmndi:BPMNEdge id="Flow_0glbdip_di" bpmnElement="Flow_0glbdip">
        <di:waypoint x="6780" y="-1950" />
        <di:waypoint x="6885" y="-1950" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/approv_basic.bpmn', '부산은행/approv_basic.bpmn', 'approv_basic.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" id="Definitions_1" name="결재 공통" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_1">
    <bpmn:participant id="Participant_1" name="결재 공통" processRef="Process_1">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:par') || TO_CLOB('ticipant>
  </bpmn:collaboration>
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties>
        <uengine:json>{"definitionName":"결재 공통","version":"0.1","shortDescription":{"text":""}}</uengine:json>
      </uengine:properties>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="결재권자">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>StartEvent_1</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>EndEvent_1</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>UserTask_1</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_0d09p0p</bpmn:flowNodeRef>
        ') || TO_CLOB('<bpmn:flowNodeRef>Activity_05lb6t3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1763qh0</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0s9e80b</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:startEvent id="StartEvent_1" name="Start">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_1" />
    <bpmn:endEvent id="EndEvent_1" name="End">
      <bpmn:incoming>Flow_16mrzgq</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1xmr6l9" sourceRef="UserTask_1" targetRef="Gateway_0d09p0p" />
    <bpmn:endEvent id="Event_0s9e80b" name="End">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"parameters":[],"checkpoints":[]') || TO_CLOB(',"dataInput":{"name":""}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0t8cpgx</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:userTask id="UserTask_1" name="결재">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_1xmr6l9</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:exclusiveGateway id="Gateway_0d09p0p">
      <bpmn:incoming>Flow_1xmr6l9</bpmn:incoming>
      <bpmn:outgoing>Flow_101qcnj</bpmn:outgoing>
      <bpmn:outgoing>Flow_1ee6wbc</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn') || TO_CLOB(':sequenceFlow id="Flow_101qcnj" name="승인" sourceRef="Gateway_0d09p0p" targetRef="Activity_05lb6t3">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"conditionMode":"text"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1ee6wbc" name="반려" sourceRef="Gateway_0d09p0p" targetRef="Activity_1763qh0">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"conditionMode":"text"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:userTask id="Activity_05lb6t3" name="승인">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[') || TO_CLOB('],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_101qcnj</bpmn:incoming>
      <bpmn:outgoing>Flow_16mrzgq</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1763qh0" name="반려">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ee6wbc</bpmn:incoming>
      <bpmn:outgoing>Flow_0t8cpgx</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_16mrzgq" sourceRef="Activity_05lb6t3" targetRef="EndEvent_1" />
    <bpmn:sequenceFlow id="Flow_0t8cpgx" sourceRef="Activity_1763qh0" t') || TO_CLOB('argetRef="Event_0s9e80b" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1">
      <bpmndi:BPMNShape id="Participant_1_di" bpmnElement="Participant_1" isHorizontal="true">
        <dc:Bounds x="160" y="140" width="570" height="230" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
        <dc:Bounds x="190" y="140" width="540" height="230" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="232" y="182" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="238" y="225" width="24" height="14" />
      ') || TO_CLOB('  </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="662" y="182" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="670" y="225" width="20" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0s9e80b_di" bpmnElement="Event_0s9e80b">
        <dc:Bounds x="662" y="292" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="670" y="335" width="20" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="UserTask_1_di" bpmnElement="UserTask_1">
        <dc:Bounds x="300" y="160" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmnd') || TO_CLOB('i:BPMNShape id="Gateway_0d09p0p_di" bpmnElement="Gateway_0d09p0p" isMarkerVisible="true">
        <dc:Bounds x="425" y="175" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_05lb6t3_di" bpmnElement="Activity_05lb6t3">
        <dc:Bounds x="520" y="160" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1763qh0_di" bpmnElement="Activity_1763qh0">
        <dc:Bounds x="520" y="270" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="268" y="200" />
        <di:waypoint x="300" y="200" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1xmr6l9_di" bpmnElement="Flow_') || TO_CLOB('1xmr6l9">
        <di:waypoint x="400" y="200" />
        <di:waypoint x="425" y="200" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_101qcnj_di" bpmnElement="Flow_101qcnj">
        <di:waypoint x="475" y="200" />
        <di:waypoint x="520" y="200" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="487" y="182" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ee6wbc_di" bpmnElement="Flow_1ee6wbc">
        <di:waypoint x="450" y="225" />
        <di:waypoint x="450" y="310" />
        <di:waypoint x="520" y="310" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="455" y="265" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_16mrzgq_di" bpmnElement') || TO_CLOB('="Flow_16mrzgq">
        <di:waypoint x="620" y="200" />
        <di:waypoint x="662" y="200" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0t8cpgx_di" bpmnElement="Flow_0t8cpgx">
        <di:waypoint x="620" y="310" />
        <di:waypoint x="662" y="310" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/credit_review.bpmn', '부산은행/credit_review.bpmn', 'credit_review.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?><bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" id="Definitions_1" name="부산은행/credit_review" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_1">
    <bpmn:participant id="Participant_1" name="여신 심사 프로세스" processRef="Process_1">
      <bpmn:extensionElements>
        <uengine:properties json="{}"/>
      </bpmn:extensionElements>
    </bpmn:participant>
  </bpmn:collaboration>
  <bpmn:pr') || TO_CLOB('ocess id="Process_1" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties json="{&quot;definitionName&quot;:&quot;부산은행/credit_review&quot;,&quot;version&quot;:&quot;0.1&quot;,&quot;shortDescription&quot;:{&quot;text&quot;:null}}"/>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="은행">
        <bpmn:extensionElements>
          <uengine:properties json="{}"/>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Event_1hq8esl</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_04803yj</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0ia8tkh</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1x06x02</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1mbvbv4</bpmn:flowNodeRef>
        <bpmn:flowNodeRef') || TO_CLOB('>Activity_0uygbh6</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1uf5yay</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_11lw9de</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0r9thy3</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:sequenceFlow id="Flow_1el3yfv" sourceRef="Event_1hq8esl" targetRef="Activity_04803yj"/>
    <bpmn:sequenceFlow id="Flow_1ha9rte" sourceRef="Activity_04803yj" targetRef="Activity_0ia8tkh"/>
    <bpmn:sequenceFlow id="Flow_1l082t5" sourceRef="Activity_0ia8tkh" targetRef="Activity_1x06x02"/>
    <bpmn:sequenceFlow id="Flow_11kin9c" sourceRef="Activity_1x06x02" targetRef="Activity_1mbvbv4"/>
    <bpmn:sequenceFlow id="Flow_0t9yo0z" sourceRef="Activity_1mbvbv4" targetRef="Activity_0uygbh6"/>
    <bpmn:sequenceFlow id="Flow_1xadvkv') || TO_CLOB('" sourceRef="Activity_0uygbh6" targetRef="Activity_1uf5yay"/>
    <bpmn:sequenceFlow id="Flow_15681d7" sourceRef="Activity_1uf5yay" targetRef="Activity_11lw9de"/>
    <bpmn:sequenceFlow id="Flow_0n01qnr" sourceRef="Activity_11lw9de" targetRef="Event_0r9thy3"/>
    <bpmn:startEvent id="Event_1hq8esl" name="시작">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1el3yfv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="Activity_04803yj" name="대출 신청 접수">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;대출 신청 접수&quot;,&quot;id&quot;:&quot;Activity_04803yj&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&lt;p&g') || TO_CLOB('t;&lt;span style=\&quot;background-color: transparent; color: rgb(0, 0, 0);\&quot;&gt;고객 기본정보와 신청상품, 신청금액/기간을 접수하고 접수 채널을 기록한다.&lt;/span&gt;&lt;/p&gt;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_04803yj&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[&quot;필수값 누락, 상품 코드/이름 불일치, 중복 신청 여부&quot;],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1el3yfv</bpmn:incoming>
      ') || TO_CLOB('<bpmn:outgoing>Flow_1ha9rte</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0ia8tkh" name="신청 정보 입력">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;신청 정보 입력&quot;,&quot;id&quot;:&quot;Activity_0ia8tkh&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_0ia8tkh&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;') || TO_CLOB('checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ha9rte</bpmn:incoming>
      <bpmn:outgoing>Flow_1l082t5</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1x06x02" name="서류 확인">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;서류 확인&quot;,&quot;id&quot;:&quot;Activity_1x06x02&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&q') || TO_CLOB('uot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1x06x02&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1l082t5</bpmn:incoming>
      <bpmn:outgoing>Flow_11kin9c</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1mbvbv4" name="신용/소득 조회">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;신용/소득 조회&quot;,&quot;id&quot;:&quot;Activity_1mbvbv4&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&q') || TO_CLOB('uot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1mbvbv4&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_11kin9c</bpmn:incoming>
      <bpmn:outgoing>Flow_0t9yo0z</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0uygbh6" name="여신 심사 등록">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;여신 심사 등록&quot;,&quot;id&quot;:&quot;Activity_0uygbh6&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&q') || TO_CLOB('uot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_0uygbh6&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0t9yo0z</bpmn:incoming>
      <bpmn:outgoing>Flow_1xadvkv</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1uf5yay" name="약정 생성">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;약정 생성&quo') || TO_CLOB('t;,&quot;id&quot;:&quot;Activity_1uf5yay&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1uf5yay&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1xadvkv</bpmn:incoming>
      <bpmn:outgoing>Flow_15681d7</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:us') || TO_CLOB('erTask id="Activity_11lw9de" name="대출  실행">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;대출  실행&quot;,&quot;id&quot;:&quot;Activity_11lw9de&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_11lw9de&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extension') || TO_CLOB('Elements>
      <bpmn:incoming>Flow_15681d7</bpmn:incoming>
      <bpmn:outgoing>Flow_0n01qnr</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0r9thy3">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0n01qnr</bpmn:incoming>
    </bpmn:endEvent>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1">
      <bpmndi:BPMNShape id="Participant_1_di" bpmnElement="Participant_1" isHorizontal="true">
        <dc:Bounds x="-30" y="0" width="1380" height="120"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
    ') || TO_CLOB('    <dc:Bounds x="0" y="0" width="1350" height="120"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1hq8esl_di" bpmnElement="Event_1hq8esl">
        <dc:Bounds x="22" y="42" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="30" y="85" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_04803yj_di" bpmnElement="Activity_04803yj">
        <dc:Bounds x="140" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0ia8tkh_di" bpmnElement="Activity_0ia8tkh">
        <dc:Bounds x="290" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id=') || TO_CLOB('"Activity_1x06x02_di" bpmnElement="Activity_1x06x02">
        <dc:Bounds x="440" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1mbvbv4_di" bpmnElement="Activity_1mbvbv4">
        <dc:Bounds x="590" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0uygbh6_di" bpmnElement="Activity_0uygbh6">
        <dc:Bounds x="740" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1uf5yay_di" bpmnElement="Activity_1uf5yay">
        <dc:Bounds x="890" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_11lw9de_di') || TO_CLOB('" bpmnElement="Activity_11lw9de">
        <dc:Bounds x="1040" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0r9thy3_di" bpmnElement="Event_0r9thy3">
        <dc:Bounds x="1222" y="42" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1el3yfv_di" bpmnElement="Flow_1el3yfv">
        <di:waypoint x="58" y="60"/>
        <di:waypoint x="140" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ha9rte_di" bpmnElement="Flow_1ha9rte">
        <di:waypoint x="240" y="60"/>
        <di:waypoint x="290" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1l082t5_di" bpmnElement="Flow_1l082t5">
        <di:waypoint x="390" y="60"/>
        <di:waypoint x="440" y="60"') || TO_CLOB('/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_11kin9c_di" bpmnElement="Flow_11kin9c">
        <di:waypoint x="540" y="60"/>
        <di:waypoint x="590" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0t9yo0z_di" bpmnElement="Flow_0t9yo0z">
        <di:waypoint x="690" y="60"/>
        <di:waypoint x="740" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1xadvkv_di" bpmnElement="Flow_1xadvkv">
        <di:waypoint x="840" y="60"/>
        <di:waypoint x="890" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_15681d7_di" bpmnElement="Flow_15681d7">
        <di:waypoint x="990" y="60"/>
        <di:waypoint x="1040" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0n01qnr_di" bpmnElement="Flow_0n01qnr">
       ') || TO_CLOB(' <di:waypoint x="1140" y="60"/>
        <di:waypoint x="1222" y="60"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/credit_review_call.bpmn', '부산은행/credit_review_call.bpmn', 'credit_review_call.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?><bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" id="Definitions_1" name="부산은행/ credit_review_call" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_1">
    <bpmn:participant id="Participant_1" name="여신 심사 프로세스" processRef="Process_1">
      <bpmn:extensionElements>
        <uengine:properties json="{}"/>
      </bpmn:extensionElements>
    </bpmn:participant>
  </bpmn:collaboration>
  <b') || TO_CLOB('pmn:process id="Process_1" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties json="{&quot;definitionName&quot;:&quot;부산은행/ credit_review_call&quot;,&quot;version&quot;:&quot;0.1&quot;,&quot;shortDescription&quot;:{&quot;text&quot;:&quot;&quot;}}"/>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="은행">
        <bpmn:extensionElements>
          <uengine:properties json="{}"/>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Event_1hq8esl</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_04803yj</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0ia8tkh</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1x06x02</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1mbvbv4</bpmn:flowNodeRef>
     ') || TO_CLOB('   <bpmn:flowNodeRef>Gateway_04rrgs9</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_11lw9de</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1uf5yay</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0r9thy3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_09o0kbr</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0tgmfcp</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1pjzit3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0uygbh6</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:sequenceFlow id="Flow_1el3yfv" sourceRef="Event_1hq8esl" targetRef="Activity_04803yj"/>
    <bpmn:sequenceFlow id="Flow_1ha9rte" sourceRef="Activity_04803yj" targetRef="Activity_0ia8tkh"/>
    <bpmn:sequenceFlow id="Flow_1l082t5" sourceRef="Activity_0ia8tkh" targetRe') || TO_CLOB('f="Activity_1x06x02"/>
    <bpmn:sequenceFlow id="Flow_11kin9c" sourceRef="Activity_1x06x02" targetRef="Activity_1mbvbv4"/>
    <bpmn:sequenceFlow id="Flow_0t9yo0z" sourceRef="Activity_1mbvbv4" targetRef="Activity_0uygbh6"/>
    <bpmn:sequenceFlow id="Flow_15681d7" sourceRef="Activity_1uf5yay" targetRef="Activity_11lw9de"/>
    <bpmn:sequenceFlow id="Flow_0n01qnr" sourceRef="Activity_11lw9de" targetRef="Event_0r9thy3"/>
    <bpmn:sequenceFlow id="Flow_00gl8rl" sourceRef="Activity_0uygbh6" targetRef="Gateway_04rrgs9"/>
    <bpmn:sequenceFlow id="Flow_0o9ucjw" name="승인" sourceRef="Gateway_04rrgs9" targetRef="Activity_1uf5yay">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;conditionMode&quot;:&quot;text&quot;}"/>
      </bpmn:extensionElements>
    </bpmn:sequenceFlo') || TO_CLOB('w>
    <bpmn:sequenceFlow id="Flow_0oxw3eb" name="반려" sourceRef="Gateway_04rrgs9" targetRef="Activity_09o0kbr">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;conditionMode&quot;:&quot;text&quot;}"/>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_1viw274" sourceRef="Activity_09o0kbr" targetRef="Event_0tgmfcp"/>
    <bpmn:sequenceFlow id="Flow_1rkq74z" name="보완 요청" sourceRef="Gateway_04rrgs9" targetRef="Activity_1pjzit3">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;conditionMode&quot;:&quot;text&quot;}"/>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_01xql2s" sourceRef="Activity_1pjzit3" targetRef="Activity_1x06x02"/>
    <bpmn:startEvent id="Event_1hq8es') || TO_CLOB('l" name="시작">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1el3yfv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="Activity_04803yj" name="대출 신청 접수">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;대출 신청 접수&quot;,&quot;id&quot;:&quot;Activity_04803yj&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quo') || TO_CLOB('t;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_04803yj&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1el3yfv</bpmn:incoming>
      <bpmn:outgoing>Flow_1ha9rte</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0ia8tkh" name="신청 정보 입력">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;신청 정보 입력&quot;,&quot;id&quot;:&quot;Activity_0ia8tkh&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;age') || TO_CLOB('ntMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_0ia8tkh&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ha9rte</bpmn:incoming>
      <bpmn:outgoing>Flow_1l082t5</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1x06x02" name="서류 확인">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;서류 확인&quot;,&quot;id&quot;:&quot;Activity_1x06x02&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;inst') || TO_CLOB('ruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1x06x02&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1l082t5</bpmn:incoming>
      <bpmn:incoming>Flow_01xql2s</bpmn:incoming>
      <bpmn:outgoing>Flow_11kin9c</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1mbvbv4" name="신용/소득 조회">
      <bpmn:extensionElements>
     ') || TO_CLOB('   <uengine:properties json="{&quot;name&quot;:&quot;신용/소득 조회&quot;,&quot;id&quot;:&quot;Activity_1mbvbv4&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1mbvbv4&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_11kin9c</bpmn:incoming>
      <bpmn:outgoi') || TO_CLOB('ng>Flow_0t9yo0z</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:exclusiveGateway id="Gateway_04rrgs9">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_00gl8rl</bpmn:incoming>
      <bpmn:outgoing>Flow_0o9ucjw</bpmn:outgoing>
      <bpmn:outgoing>Flow_0oxw3eb</bpmn:outgoing>
      <bpmn:outgoing>Flow_1rkq74z</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:userTask id="Activity_11lw9de" name="대출  실행">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;대출  실행&quot;,&quot;id&quot;:&quot;Activity_11lw9de&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&') || TO_CLOB('quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_11lw9de&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_15681d7</bpmn:incoming>
      <bpmn:outgoing>Flow_0n01qnr</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1uf5yay" name="약정 생성">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;약정 생성&quot;,&quot;id&quot;:&quot;Activity_1uf5ya') || TO_CLOB('y&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1uf5yay&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0o9ucjw</bpmn:incoming>
      <bpmn:outgoing>Flow_15681d7</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0r9thy3" name="종료">
  ') || TO_CLOB('    <bpmn:extensionElements>
        <uengine:properties json="{&quot;parameters&quot;:[],&quot;checkpoints&quot;:[],&quot;dataInput&quot;:{&quot;name&quot;:&quot;&quot;},&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0n01qnr</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:userTask id="Activity_09o0kbr" name="반려 통지">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;반려 통지&quot;,&quot;id&quot;:&quot;Activity_09o0kbr&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;') || TO_CLOB(',&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_09o0kbr&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0oxw3eb</bpmn:incoming>
      <bpmn:outgoing>Flow_1viw274</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0tgmfcp" name="종료">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;parameters&quot;:[],&quot;checkpoints&quot;:[],&quot;dataInput&quot;:{&quot;name&quot;:&quot;&quot;},&quot;role&quot;:&quot;은행&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1viw274</bpmn:incoming>
    </bpm') || TO_CLOB('n:endEvent>
    <bpmn:userTask id="Activity_1pjzit3" name="보완 요청">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;보완 요청&quot;,&quot;id&quot;:&quot;Activity_1pjzit3&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quot;Activity_1pjzit3&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;}"/>
') || TO_CLOB('      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1rkq74z</bpmn:incoming>
      <bpmn:outgoing>Flow_01xql2s</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:callActivity id="Activity_0uygbh6" name="여신 심사">
      <bpmn:extensionElements>
        <uengine:properties json="{&quot;name&quot;:&quot;여신 심사&quot;,&quot;id&quot;:&quot;Activity_0uygbh6&quot;,&quot;type&quot;:&quot;userTask&quot;,&quot;description&quot;:&quot;&quot;,&quot;instruction&quot;:&quot;&quot;,&quot;process&quot;:&quot;Process_1&quot;,&quot;attachedEvents&quot;:null,&quot;agent&quot;:&quot;&quot;,&quot;agentMode&quot;:&quot;&quot;,&quot;orchestration&quot;:&quot;&quot;,&quot;attachments&quot;:[],&quot;inputData&quot;:[],&quot;outputData&quot;:[],&quot;tool&quot;:&quot;formHandler:defaultform&quot;,&quot;uuid&quot;:&quo') || TO_CLOB('t;Activity_0uygbh6&quot;,&quot;role&quot;:&quot;은행&quot;,&quot;duration&quot;:5,&quot;checkpoints&quot;:[],&quot;taskLink&quot;:&quot;&quot;,&quot;variableBindings&quot;:[],&quot;roleBindings&quot;:[],&quot;definitionId&quot;:&quot;부산은행/approv_basic.bpmn&quot;,&quot;customProperties&quot;:[],&quot;version&quot;:&quot;0.1&quot;}"/>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0t9yo0z</bpmn:incoming>
      <bpmn:outgoing>Flow_00gl8rl</bpmn:outgoing>
    </bpmn:callActivity>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1">
      <bpmndi:BPMNShape id="Participant_1_di" bpmnElement="Participant_1" isHorizontal="true">
        <dc:Bounds x="-30" y="0" width="1530" height="360"/>
        <bpmndi:BPMNLabe') || TO_CLOB('l/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
        <dc:Bounds x="0" y="0" width="1500" height="360"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1hq8esl_di" bpmnElement="Event_1hq8esl">
        <dc:Bounds x="22" y="42" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="30" y="85" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_04803yj_di" bpmnElement="Activity_04803yj">
        <dc:Bounds x="140" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0ia8tkh_di" bpmnElement="Activity_0ia8tkh">
        <dc:Bounds x="290"') || TO_CLOB(' y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1x06x02_di" bpmnElement="Activity_1x06x02">
        <dc:Bounds x="440" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1mbvbv4_di" bpmnElement="Activity_1mbvbv4">
        <dc:Bounds x="590" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_04rrgs9_di" bpmnElement="Gateway_04rrgs9" isMarkerVisible="true">
        <dc:Bounds x="915" y="35" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_11lw9de_di" bpmnElement="Activity_11lw9de">
        <dc:Bounds x="1190" y="20" width="100" height=') || TO_CLOB('"80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1uf5yay_di" bpmnElement="Activity_1uf5yay">
        <dc:Bounds x="1040" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0r9thy3_di" bpmnElement="Event_0r9thy3">
        <dc:Bounds x="1372" y="42" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1380" y="85" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_09o0kbr_di" bpmnElement="Activity_09o0kbr">
        <dc:Bounds x="1040" y="140" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0tgmfcp_di" bpmnElement="Event_0tg') || TO_CLOB('mfcp">
        <dc:Bounds x="1222" y="162" width="36" height="36"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1230" y="205" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1pjzit3_di" bpmnElement="Activity_1pjzit3">
        <dc:Bounds x="1040" y="260" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_00hv9z0_di" bpmnElement="Activity_0uygbh6">
        <dc:Bounds x="740" y="20" width="100" height="80"/>
        <bpmndi:BPMNLabel/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1el3yfv_di" bpmnElement="Flow_1el3yfv">
        <di:waypoint x="58" y="60"/>
        <di:waypoint x="140" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge i') || TO_CLOB('d="Flow_1ha9rte_di" bpmnElement="Flow_1ha9rte">
        <di:waypoint x="240" y="60"/>
        <di:waypoint x="290" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1l082t5_di" bpmnElement="Flow_1l082t5">
        <di:waypoint x="390" y="60"/>
        <di:waypoint x="440" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_11kin9c_di" bpmnElement="Flow_11kin9c">
        <di:waypoint x="540" y="60"/>
        <di:waypoint x="590" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0t9yo0z_di" bpmnElement="Flow_0t9yo0z">
        <di:waypoint x="690" y="60"/>
        <di:waypoint x="740" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_15681d7_di" bpmnElement="Flow_15681d7">
        <di:waypoint x="1140" y="60"/>
        <di:waypoint ') || TO_CLOB('x="1190" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0n01qnr_di" bpmnElement="Flow_0n01qnr">
        <di:waypoint x="1290" y="60"/>
        <di:waypoint x="1372" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_00gl8rl_di" bpmnElement="Flow_00gl8rl">
        <di:waypoint x="840" y="60"/>
        <di:waypoint x="915" y="60"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0o9ucjw_di" bpmnElement="Flow_0o9ucjw">
        <di:waypoint x="965" y="60"/>
        <di:waypoint x="1040" y="60"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="992" y="42" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0oxw3eb_di" bpmnElement="Flow_0oxw3eb">
        <di:waypoint x="965" y="60"/>
        <') || TO_CLOB('di:waypoint x="980" y="60"/>
        <di:waypoint x="980" y="180"/>
        <di:waypoint x="1040" y="180"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="985" y="120" width="21" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1viw274_di" bpmnElement="Flow_1viw274">
        <di:waypoint x="1140" y="180"/>
        <di:waypoint x="1222" y="180"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1rkq74z_di" bpmnElement="Flow_1rkq74z">
        <di:waypoint x="965" y="60"/>
        <di:waypoint x="980" y="60"/>
        <di:waypoint x="980" y="300"/>
        <di:waypoint x="1040" y="300"/>
        <bpmndi:BPMNLabel>
          <dc:Bounds x="973" y="179" width="44" height="14"/>
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
    ') || TO_CLOB('  <bpmndi:BPMNEdge id="Flow_01xql2s_di" bpmnElement="Flow_01xql2s">
        <di:waypoint x="1090" y="260"/>
        <di:waypoint x="1090" y="250"/>
        <di:waypoint x="560" y="250"/>
        <di:waypoint x="560" y="60"/>
        <di:waypoint x="540" y="60"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/credit_review_gateway.bpmn', '부산은행/credit_review_gateway.bpmn', 'credit_review_gateway.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" id="Definitions_1" name="부산은행/credit_review_gateway" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_1">
    <bpmn:participant id="Participant_1" name="여신 심사 프로세스" processRef="Process_1">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensi') || TO_CLOB('onElements>
    </bpmn:participant>
  </bpmn:collaboration>
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties>
        <uengine:json>{"definitionName":"부산은행/credit_review_gateway","version":"0.2","shortDescription":{"text":null}}</uengine:json>
      </uengine:properties>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="은행">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Event_1hq8esl</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_04803yj</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0ia8tkh</bpmn:flowNodeRef>
        <') || TO_CLOB('bpmn:flowNodeRef>Activity_1x06x02</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1mbvbv4</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0uygbh6</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_04rrgs9</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_11lw9de</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1uf5yay</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0r9thy3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_09o0kbr</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0tgmfcp</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1pjzit3</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:startEvent id="Event_1hq8esl" name="시작">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"role":"은행"}</uengine') || TO_CLOB(':json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1el3yfv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:sequenceFlow id="Flow_1el3yfv" sourceRef="Event_1hq8esl" targetRef="Activity_04803yj" />
    <bpmn:sequenceFlow id="Flow_1ha9rte" sourceRef="Activity_04803yj" targetRef="Activity_0ia8tkh" />
    <bpmn:sequenceFlow id="Flow_1l082t5" sourceRef="Activity_0ia8tkh" targetRef="Activity_1x06x02" />
    <bpmn:sequenceFlow id="Flow_11kin9c" sourceRef="Activity_1x06x02" targetRef="Activity_1mbvbv4" />
    <bpmn:sequenceFlow id="Flow_0t9yo0z" sourceRef="Activity_1mbvbv4" targetRef="Activity_0uygbh6" />
    <bpmn:sequenceFlow id="Flow_15681d7" sourceRef="Activity_1uf5yay" targetRef="Activity_11lw9de" />
    <bpmn:sequenceFlow id="Flow_0n01qnr" sourceRe') || TO_CLOB('f="Activity_11lw9de" targetRef="Event_0r9thy3" />
    <bpmn:sequenceFlow id="Flow_00gl8rl" sourceRef="Activity_0uygbh6" targetRef="Gateway_04rrgs9" />
    <bpmn:userTask id="Activity_04803yj" name="대출 신청 접수">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"대출 신청 접수","id":"Activity_04803yj","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_04803yj","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1el3yfv</bpmn:incoming>
      <bpmn:outgoing>Flow_1ha9rt') || TO_CLOB('e</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0ia8tkh" name="신청 정보 입력">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"신청 정보 입력","id":"Activity_0ia8tkh","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_0ia8tkh","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ha9rte</bpmn:incoming>
      <bpmn:outgoing>Flow_1l082t5</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1x06x02" name="서류 확인">
      <bpmn:extens') || TO_CLOB('ionElements>
        <uengine:properties>
          <uengine:json>{"name":"서류 확인","id":"Activity_1x06x02","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_1x06x02","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1l082t5</bpmn:incoming>
      <bpmn:incoming>Flow_01xql2s</bpmn:incoming>
      <bpmn:outgoing>Flow_11kin9c</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1mbvbv4" name="신용/소득 조회">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json') || TO_CLOB('>{"name":"신용/소득 조회","id":"Activity_1mbvbv4","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_1mbvbv4","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_11kin9c</bpmn:incoming>
      <bpmn:outgoing>Flow_0t9yo0z</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0uygbh6" name="여신 심사">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"여신 심사","id":"Activity_0uygbh6","type":"userTask","description":"","instruction":"","process":"Process_1",') || TO_CLOB('"attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_0uygbh6","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0t9yo0z</bpmn:incoming>
      <bpmn:outgoing>Flow_00gl8rl</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:exclusiveGateway id="Gateway_04rrgs9">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"role":"은행"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_00gl8rl</bpmn:incoming>
      <bpmn:outgoing>Flow_0o9ucjw</bpmn:outgoing>
      <bpmn:outgoing>Flow_0oxw3eb</bpmn:outgoin') || TO_CLOB('g>
      <bpmn:outgoing>Flow_1rkq74z</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sequenceFlow id="Flow_0o9ucjw" name="승인" sourceRef="Gateway_04rrgs9" targetRef="Activity_1uf5yay">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"conditionMode":"text"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:userTask id="Activity_11lw9de" name="대출  실행">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"대출  실행","id":"Activity_11lw9de","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultf') || TO_CLOB('orm","uuid":"Activity_11lw9de","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_15681d7</bpmn:incoming>
      <bpmn:outgoing>Flow_0n01qnr</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1uf5yay" name="약정 생성">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"약정 생성","id":"Activity_1uf5yay","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_1uf5yay","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengi') || TO_CLOB('ne:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0o9ucjw</bpmn:incoming>
      <bpmn:outgoing>Flow_15681d7</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0r9thy3" name="종료">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"parameters":[],"checkpoints":[],"dataInput":{"name":""},"role":"은행"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0n01qnr</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:userTask id="Activity_09o0kbr" name="반려 통지">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"반려 통지","id":"Activity_09o0kbr","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agen') || TO_CLOB('t":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_09o0kbr","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0oxw3eb</bpmn:incoming>
      <bpmn:outgoing>Flow_1viw274</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_0oxw3eb" sourceRef="Gateway_04rrgs9" targetRef="Activity_09o0kbr" />
    <bpmn:endEvent id="Event_0tgmfcp" name="종료">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"parameters":[],"checkpoints":[],"dataInput":{"name":""},"role":"은행"}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:inco') || TO_CLOB('ming>Flow_1viw274</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1viw274" sourceRef="Activity_09o0kbr" targetRef="Event_0tgmfcp" />
    <bpmn:userTask id="Activity_1pjzit3" name="보완 요청">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"name":"보완 요청","id":"Activity_1pjzit3","type":"userTask","description":"","instruction":"","process":"Process_1","attachedEvents":null,"agent":"","agentMode":"","orchestration":"","attachments":[],"inputData":[],"outputData":[],"tool":"formHandler:defaultform","uuid":"Activity_1pjzit3","role":"은행","duration":5,"checkpoints":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1rkq74z</bpmn:incoming>
      <bpmn:outgoing>Flow_01xql2s</') || TO_CLOB('bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_1rkq74z" sourceRef="Gateway_04rrgs9" targetRef="Activity_1pjzit3" />
    <bpmn:sequenceFlow id="Flow_01xql2s" sourceRef="Activity_1pjzit3" targetRef="Activity_1x06x02" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1">
      <bpmndi:BPMNShape id="Participant_1_di" bpmnElement="Participant_1" isHorizontal="true">
        <dc:Bounds x="160" y="80" width="1240" height="340" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
        <dc:Bounds x="190" y="80" width="1210" height="340" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNS') || TO_CLOB('hape id="Event_1hq8esl_di" bpmnElement="Event_1hq8esl">
        <dc:Bounds x="232" y="112" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="240" y="155" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_04803yj_di" bpmnElement="Activity_04803yj">
        <dc:Bounds x="300" y="90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0ia8tkh_di" bpmnElement="Activity_0ia8tkh">
        <dc:Bounds x="430" y="90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1x06x02_di" bpmnElement="Activity_1x06x02">
        <dc:Bounds x="560" y="90" width="100" height="80" />
   ') || TO_CLOB('     <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1mbvbv4_di" bpmnElement="Activity_1mbvbv4">
        <dc:Bounds x="690" y="90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0uygbh6_di" bpmnElement="Activity_0uygbh6">
        <dc:Bounds x="820" y="90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_04rrgs9_di" bpmnElement="Gateway_04rrgs9" isMarkerVisible="true">
        <dc:Bounds x="945" y="105" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_11lw9de_di" bpmnElement="Activity_11lw9de">
        <dc:Bounds x="1190" y="90" width="100" height="80" />
        <bpmndi:BPMNLa') || TO_CLOB('bel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1uf5yay_di" bpmnElement="Activity_1uf5yay">
        <dc:Bounds x="1050" y="90" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0r9thy3_di" bpmnElement="Event_0r9thy3">
        <dc:Bounds x="1322" y="112" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1330" y="155" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_09o0kbr_di" bpmnElement="Activity_09o0kbr">
        <dc:Bounds x="1050" y="200" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0tgmfcp_di" bpmnElement="Event_0tgmfcp">
        <dc:B') || TO_CLOB('ounds x="1212" y="222" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1220" y="265" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1pjzit3_di" bpmnElement="Activity_1pjzit3">
        <dc:Bounds x="1050" y="310" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1el3yfv_di" bpmnElement="Flow_1el3yfv">
        <di:waypoint x="268" y="130" />
        <di:waypoint x="300" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ha9rte_di" bpmnElement="Flow_1ha9rte">
        <di:waypoint x="400" y="130" />
        <di:waypoint x="430" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1l082t5_di" bpmnEleme') || TO_CLOB('nt="Flow_1l082t5">
        <di:waypoint x="530" y="130" />
        <di:waypoint x="560" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_11kin9c_di" bpmnElement="Flow_11kin9c">
        <di:waypoint x="660" y="130" />
        <di:waypoint x="690" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0t9yo0z_di" bpmnElement="Flow_0t9yo0z">
        <di:waypoint x="790" y="130" />
        <di:waypoint x="820" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_15681d7_di" bpmnElement="Flow_15681d7">
        <di:waypoint x="1150" y="130" />
        <di:waypoint x="1190" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0n01qnr_di" bpmnElement="Flow_0n01qnr">
        <di:waypoint x="1290" y="130" />
        <di:waypoint x="1322" ') || TO_CLOB('y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_00gl8rl_di" bpmnElement="Flow_00gl8rl">
        <di:waypoint x="920" y="130" />
        <di:waypoint x="945" y="130" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0o9ucjw_di" bpmnElement="Flow_0o9ucjw">
        <di:waypoint x="995" y="130" />
        <di:waypoint x="1050" y="130" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1012" y="112" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0oxw3eb_di" bpmnElement="Flow_0oxw3eb">
        <di:waypoint x="970" y="155" />
        <di:waypoint x="970" y="240" />
        <di:waypoint x="1050" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1viw274_di" bpmnElement="Flow_1viw274">
') || TO_CLOB('        <di:waypoint x="1150" y="240" />
        <di:waypoint x="1212" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1rkq74z_di" bpmnElement="Flow_1rkq74z">
        <di:waypoint x="970" y="155" />
        <di:waypoint x="970" y="350" />
        <di:waypoint x="1050" y="350" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_01xql2s_di" bpmnElement="Flow_01xql2s">
        <di:waypoint x="1100" y="390" />
        <di:waypoint x="1100" y="410" />
        <di:waypoint x="610" y="410" />
        <di:waypoint x="610" y="170" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/credit_review_phase.bpmn', '부산은행/credit_review_phase.bpmn', 'credit_review_phase.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" xmlns:phase="http://example.com/phase" id="Definitions_1" name="부산은행/credit_review_phase" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_1">
    <bpmn:participant id="Participant_1" name="부산은행/credit_review_phase" processRef="Process_1">
      <bpmn:extensionElements>
        <uengine:properties />
      </bpmn:extensionElements>
    </b') || TO_CLOB('pmn:participant>
    <phase:PhaseContainer id="PhaseContainer_rx5686ta5" processRef="Process_5ra7m2u5m" numPhases="3" />
  </bpmn:collaboration>
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties json="{&#34;definitionName&#34;:&#34;부산은행/credit_review_phase&#34;,&#34;version&#34;:&#34;0.9&#34;,&#34;shortDescription&#34;:{&#34;text&#34;:null}}" />
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="은행">
        <bpmn:extensionElements>
          <uengine:properties />
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Event_1hq8esl</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_04803yj</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0ia8tkh</bpmn:flowNodeRef>
        ') || TO_CLOB('<bpmn:flowNodeRef>Activity_1x06x02</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1mbvbv4</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_0uygbh6</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Gateway_04rrgs9</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_11lw9de</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1uf5yay</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0r9thy3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_09o0kbr</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0tgmfcp</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1pjzit3</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:sequenceFlow id="Flow_1el3yfv" sourceRef="Event_1hq8esl" targetRef="Activity_04803yj" />
    <bpmn:sequenceFlow id="Flow_1ha9rte" sourceRef="Activ') || TO_CLOB('ity_04803yj" targetRef="Activity_0ia8tkh" />
    <bpmn:sequenceFlow id="Flow_1l082t5" sourceRef="Activity_0ia8tkh" targetRef="Activity_1x06x02" />
    <bpmn:sequenceFlow id="Flow_11kin9c" sourceRef="Activity_1x06x02" targetRef="Activity_1mbvbv4" />
    <bpmn:sequenceFlow id="Flow_0t9yo0z" sourceRef="Activity_1mbvbv4" targetRef="Activity_0uygbh6" />
    <bpmn:sequenceFlow id="Flow_15681d7" sourceRef="Activity_1uf5yay" targetRef="Activity_11lw9de" />
    <bpmn:sequenceFlow id="Flow_0n01qnr" sourceRef="Activity_11lw9de" targetRef="Event_0r9thy3" />
    <bpmn:sequenceFlow id="Flow_00gl8rl" sourceRef="Activity_0uygbh6" targetRef="Gateway_04rrgs9" />
    <bpmn:sequenceFlow id="Flow_0o9ucjw" name="승인" sourceRef="Gateway_04rrgs9" targetRef="Activity_1uf5yay">
      <bpmn:extensionElements>
       ') || TO_CLOB(' <uengine:properties />
      </bpmn:extensionElements>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_0oxw3eb" sourceRef="Gateway_04rrgs9" targetRef="Activity_09o0kbr" />
    <bpmn:sequenceFlow id="Flow_1viw274" sourceRef="Activity_09o0kbr" targetRef="Event_0tgmfcp" />
    <bpmn:sequenceFlow id="Flow_1rkq74z" sourceRef="Gateway_04rrgs9" targetRef="Activity_1pjzit3" />
    <bpmn:sequenceFlow id="Flow_01xql2s" sourceRef="Activity_1pjzit3" targetRef="Activity_1x06x02" />
    <bpmn:startEvent id="Event_1hq8esl" name="시작">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;role&#34;:&#34;은행&#34;}" />
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1el3yfv</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="Activity_04803yj" name="대출 신청 접수">
   ') || TO_CLOB('   <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1el3yfv</bpmn:incoming>
      <bpmn:outgoing>Flow_1ha9rte</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0ia8tkh" name="신청 정보 입력">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#') || TO_CLOB('34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ha9rte</bpmn:incoming>
      <bpmn:outgoing>Flow_1l082t5</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1x06x02" name="서류 확인">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1l082t5</bpmn:incoming>
      <bpmn:incoming>Flow_01xql2s</bpmn:') || TO_CLOB('incoming>
      <bpmn:outgoing>Flow_11kin9c</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1mbvbv4" name="신용/소득 조회">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_11kin9c</bpmn:incoming>
      <bpmn:outgoing>Flow_0t9yo0z</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_0uygbh6" name="여신 심사">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,') || TO_CLOB('&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0t9yo0z</bpmn:incoming>
      <bpmn:outgoing>Flow_00gl8rl</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:exclusiveGateway id="Gateway_04rrgs9">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;role&#34;:&#34;은행&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_00gl8rl</bpmn:incoming>
      <bpmn:outgoing>Flow_0o9ucjw</bpmn:outgoing>
      <bpmn:outgoing>Flow_0oxw3eb</bpmn:outgoing>
      <bpmn:outgoing>Flow_1rkq74z</bpmn:outgoing>
    </bpmn:e') || TO_CLOB('xclusiveGateway>
    <bpmn:userTask id="Activity_11lw9de" name="대출  실행">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_15681d7</bpmn:incoming>
      <bpmn:outgoing>Flow_0n01qnr</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_1uf5yay" name="약정 생성">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;은행&#34;,&#34;') || TO_CLOB('process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0o9ucjw</bpmn:incoming>
      <bpmn:outgoing>Flow_15681d7</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0r9thy3" name="종료">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;role&#34;:&#34;은행&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0n01qnr</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:userTask id="Activity_09o0kbr" name="반려 통지">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;role&#34;:&#34;') || TO_CLOB('은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0oxw3eb</bpmn:incoming>
      <bpmn:outgoing>Flow_1viw274</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="Event_0tgmfcp" name="종료">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;role&#34;:&#34;은행&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1viw274</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:userTask id="Activity_1pjzit3" name="보완 요청">
      <bpmn:extensionElements>
        <uengine:properties json="{&#34;description&#34;:&#34;&#34;,&#34;instruction&#34;:&#34;&#34;,&#34;ro') || TO_CLOB('le&#34;:&#34;은행&#34;,&#34;process&#34;:&#34;Process_1&#34;,&#34;inputData&#34;:[],&#34;outputData&#34;:[],&#34;properties&#34;:&#34;{}&#34;,&#34;duration&#34;:5,&#34;tool&#34;:&#34;formHandler:defaultform&#34;}" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1rkq74z</bpmn:incoming>
      <bpmn:outgoing>Flow_01xql2s</bpmn:outgoing>
    </bpmn:userTask>
  </bpmn:process>
  <bpmn:process id="Process_5ra7m2u5m" isExecutable="false">
    <bpmn:laneSet id="LaneSet_zexbv99u9">
      <phase:Phase id="Phase_edqqoturh" name="접수 단계" />
      <phase:Phase id="Phase_2omiy2zdx" name="심사 단계" />
      <phase:Phase id="Phase_r2g94vloa" name="약정/실행 단계" />
    </bpmn:laneSet>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collabor') || TO_CLOB('ation_1">
      <bpmndi:BPMNShape id="Participant_1_di" bpmnElement="Participant_1" isHorizontal="true">
        <dc:Bounds x="-30" y="0" width="1530" height="360" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
        <dc:Bounds x="0" y="0" width="1500" height="360" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1hq8esl_di" bpmnElement="Event_1hq8esl">
        <dc:Bounds x="22" y="42" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="30" y="85" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_04803yj_di" bpmnElement="Activity_04803yj">
        <dc:Bounds x="140') || TO_CLOB('" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0ia8tkh_di" bpmnElement="Activity_0ia8tkh">
        <dc:Bounds x="290" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1x06x02_di" bpmnElement="Activity_1x06x02">
        <dc:Bounds x="440" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1mbvbv4_di" bpmnElement="Activity_1mbvbv4">
        <dc:Bounds x="590" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0uygbh6_di" bpmnElement="Activity_0uygbh6">
        <dc:Bounds x="740" y="20" wid') || TO_CLOB('th="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_04rrgs9_di" bpmnElement="Gateway_04rrgs9" isMarkerVisible="true">
        <dc:Bounds x="915" y="35" width="50" height="50" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_11lw9de_di" bpmnElement="Activity_11lw9de">
        <dc:Bounds x="1190" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1uf5yay_di" bpmnElement="Activity_1uf5yay">
        <dc:Bounds x="1040" y="20" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0r9thy3_di" bpmnElement="Event_0r9thy3">
        <dc:Bounds x="1372" y="42" width="36" height="36" />
 ') || TO_CLOB('       <bpmndi:BPMNLabel>
          <dc:Bounds x="1380" y="85" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_09o0kbr_di" bpmnElement="Activity_09o0kbr">
        <dc:Bounds x="1040" y="140" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0tgmfcp_di" bpmnElement="Event_0tgmfcp">
        <dc:Bounds x="1222" y="162" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1230" y="205" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1pjzit3_di" bpmnElement="Activity_1pjzit3">
        <dc:Bounds x="1040" y="260" width="100" height="80" />
        <bpmndi:BPMNLabel />
   ') || TO_CLOB('   </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1el3yfv_di" bpmnElement="Flow_1el3yfv">
        <di:waypoint x="58" y="60" />
        <di:waypoint x="140" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ha9rte_di" bpmnElement="Flow_1ha9rte">
        <di:waypoint x="240" y="60" />
        <di:waypoint x="290" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1l082t5_di" bpmnElement="Flow_1l082t5">
        <di:waypoint x="390" y="60" />
        <di:waypoint x="440" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_11kin9c_di" bpmnElement="Flow_11kin9c">
        <di:waypoint x="540" y="60" />
        <di:waypoint x="590" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0t9yo0z_di" bpmnElement="Flow_0t9yo0z">
      ') || TO_CLOB('  <di:waypoint x="690" y="60" />
        <di:waypoint x="740" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_15681d7_di" bpmnElement="Flow_15681d7">
        <di:waypoint x="1140" y="60" />
        <di:waypoint x="1190" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0n01qnr_di" bpmnElement="Flow_0n01qnr">
        <di:waypoint x="1290" y="60" />
        <di:waypoint x="1372" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_00gl8rl_di" bpmnElement="Flow_00gl8rl">
        <di:waypoint x="840" y="60" />
        <di:waypoint x="915" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0o9ucjw_di" bpmnElement="Flow_0o9ucjw">
        <di:waypoint x="965" y="60" />
        <di:waypoint x="1040" y="60" />
        <bpmndi:BPMNLab') || TO_CLOB('el>
          <dc:Bounds x="992" y="42" width="21" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0oxw3eb_di" bpmnElement="Flow_0oxw3eb">
        <di:waypoint x="965" y="60" />
        <di:waypoint x="980" y="60" />
        <di:waypoint x="980" y="180" />
        <di:waypoint x="1040" y="180" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1viw274_di" bpmnElement="Flow_1viw274">
        <di:waypoint x="1140" y="180" />
        <di:waypoint x="1222" y="180" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1rkq74z_di" bpmnElement="Flow_1rkq74z">
        <di:waypoint x="965" y="60" />
        <di:waypoint x="980" y="60" />
        <di:waypoint x="980" y="300" />
        <di:waypoint x="1040" y="300" />
      </bpmndi:') || TO_CLOB('BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_01xql2s_di" bpmnElement="Flow_01xql2s">
        <di:waypoint x="1090" y="260" />
        <di:waypoint x="1090" y="250" />
        <di:waypoint x="560" y="250" />
        <di:waypoint x="560" y="60" />
        <di:waypoint x="540" y="60" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNShape id="PhaseContainer_rx5686ta5_di" bpmnElement="PhaseContainer_rx5686ta5" isHorizontal="false">
        <dc:Bounds x="-30" y="-60" width="1530" height="60" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Phase_edqqoturh_di" bpmnElement="Phase_edqqoturh" isHorizontal="false">
        <dc:Bounds x="-30" y="-60" width="440" height="60" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Phase_2omiy2zdx_di" bpmnElement="Phase_2o') || TO_CLOB('miy2zdx" isHorizontal="false">
        <dc:Bounds x="410" y="-60" width="590" height="60" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Phase_r2g94vloa_di" bpmnElement="Phase_r2g94vloa" isHorizontal="false">
        <dc:Bounds x="1000" y="-60" width="500" height="60" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
'), '20250311000000', '20250311000000');

INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)
VALUES ('부산은행/offering.bpmn', '부산은행/offering.bpmn', 'offering.bpmn', 0, 'bpmn', TO_CLOB('<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:uengine="http://uengine" id="Definitions_1" name="헌금하기" targetNamespace="http://bpmn.io/schema/bpmn" exporter="bpmn-js (https://demo.bpmn.io)" exporterVersion="16.4.0">
  <bpmn:collaboration id="Collaboration_0vitwct">
    <bpmn:participant id="Participant_05zgvpg" name="헌금하기" processRef="Process_1h0b9bc">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements') || TO_CLOB('>
    </bpmn:participant>
  </bpmn:collaboration>
  <bpmn:process id="Process_1h0b9bc" isExecutable="true">
    <bpmn:extensionElements>
      <uengine:properties>
        <uengine:json>{"definitionName":"헌금하기","version":"0.1","shortDescription":{"text":""}}</uengine:json>
      </uengine:properties>
    </bpmn:extensionElements>
    <bpmn:laneSet id="LaneSet_0spma55">
      <bpmn:lane id="Lane_0w6t4p8" name="사용자">
        <bpmn:extensionElements>
          <uengine:properties>
            <uengine:json>{}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Activity_03r05hb</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_0k7urnm</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_0iudowc" name="은행">
        <bpmn:extensi') || TO_CLOB('onElements>
          <uengine:properties>
            <uengine:json>{}</uengine:json>
          </uengine:properties>
        </bpmn:extensionElements>
        <bpmn:flowNodeRef>Activity_1n7g446</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_13jezmb</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_1gyxbkk</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Activity_01qxjba</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Event_08gmq7s</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:startEvent id="Event_0k7urnm" name="시작">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1sh7shl</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:use') || TO_CLOB('rTask id="Activity_03r05hb" name="헌금 정보 입력">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1sh7shl</bpmn:incoming>
      <bpmn:outgoing>Flow_18alxz2</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_1sh7shl" sourceRef="Event_0k7urnm" targetRef="Activity_03r05hb" />
    <bpmn:sequenceFlow id="Flow_18alxz2" sourceRef="Activity_03r05hb" targetRef="Activity_1n7g446" />
    <bpmn:userTask id="Activity_1n7g446" name="입력값 검증">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""') || TO_CLOB('}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_18alxz2</bpmn:incoming>
      <bpmn:outgoing>Flow_1jky0hh</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:userTask id="Activity_13jezmb" name="헌금 접수 등록">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1jky0hh</bpmn:incoming>
      <bpmn:outgoing>Flow_0iykufx</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_1jky0hh" sourceRef="Activity_1n7g446" targetRef="Activity_13jezmb" />
    <bpmn:userTask id="Activity_1gyxbkk" name="영수증/안내 방송">
      <bpmn:extensionElements>
 ') || TO_CLOB('       <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0iykufx</bpmn:incoming>
      <bpmn:outgoing>Flow_1ah78f8</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_0iykufx" sourceRef="Activity_13jezmb" targetRef="Activity_1gyxbkk" />
    <bpmn:userTask id="Activity_01qxjba" name="처리 완료 기록">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"description":"","checkpoints":[],"attachments":[],"taskLink":""}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1ah78f8</bpmn:incoming>
      <bpmn:outgoing>Flow_17i1r09</bpmn:outgo') || TO_CLOB('ing>
    </bpmn:userTask>
    <bpmn:sequenceFlow id="Flow_1ah78f8" sourceRef="Activity_1gyxbkk" targetRef="Activity_01qxjba" />
    <bpmn:endEvent id="Event_08gmq7s" name="끝">
      <bpmn:extensionElements>
        <uengine:properties>
          <uengine:json>{"parameters":[],"checkpoints":[],"dataInput":{"name":""}}</uengine:json>
        </uengine:properties>
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_17i1r09</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_17i1r09" sourceRef="Activity_01qxjba" targetRef="Event_08gmq7s" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_0vitwct">
      <bpmndi:BPMNShape id="Participant_05zgvpg_di" bpmnElement="Participant_05zgvpg" isHorizontal') || TO_CLOB('="true">
        <dc:Bounds x="270" y="40" width="1020" height="200" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0w6t4p8_di" bpmnElement="Lane_0w6t4p8" isHorizontal="true">
        <dc:Bounds x="300" y="40" width="990" height="100" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_0iudowc_di" bpmnElement="Lane_0iudowc" isHorizontal="true">
        <dc:Bounds x="300" y="140" width="990" height="100" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_0k7urnm_di" bpmnElement="Event_0k7urnm">
        <dc:Bounds x="332" y="72" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="340" y="115" width="21" height="14" />
        </bpmndi:BPMNLabel>') || TO_CLOB('
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_03r05hb_di" bpmnElement="Activity_03r05hb">
        <dc:Bounds x="420" y="50" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1n7g446_di" bpmnElement="Activity_1n7g446">
        <dc:Bounds x="580" y="150" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_13jezmb_di" bpmnElement="Activity_13jezmb">
        <dc:Bounds x="740" y="150" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1gyxbkk_di" bpmnElement="Activity_1gyxbkk">
        <dc:Bounds x="900" y="150" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </') || TO_CLOB('bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_01qxjba_di" bpmnElement="Activity_01qxjba">
        <dc:Bounds x="1060" y="150" width="100" height="80" />
        <bpmndi:BPMNLabel />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_08gmq7s_di" bpmnElement="Event_08gmq7s">
        <dc:Bounds x="1222" y="172" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1235" y="215" width="11" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1sh7shl_di" bpmnElement="Flow_1sh7shl">
        <di:waypoint x="368" y="90" />
        <di:waypoint x="420" y="90" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_18alxz2_di" bpmnElement="Flow_18alxz2">
        <di:waypoint x="520" y="90" />
        <di:way') || TO_CLOB('point x="550" y="90" />
        <di:waypoint x="550" y="190" />
        <di:waypoint x="580" y="190" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1jky0hh_di" bpmnElement="Flow_1jky0hh">
        <di:waypoint x="680" y="190" />
        <di:waypoint x="740" y="190" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0iykufx_di" bpmnElement="Flow_0iykufx">
        <di:waypoint x="840" y="190" />
        <di:waypoint x="900" y="190" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1ah78f8_di" bpmnElement="Flow_1ah78f8">
        <di:waypoint x="1000" y="190" />
        <di:waypoint x="1060" y="190" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_17i1r09_di" bpmnElement="Flow_17i1r09">
        <di:waypoint x="1160" y="190" />
        <di:waypoint x="1') || TO_CLOB('222" y="190" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
'), '20250311000000', '20250311000000');

COMMIT;
SELECT COUNT(*) AS inserted FROM TB_BPM_PROCDEF WHERE path LIKE '부산은행/%';