package nmka.handler;

import nmka.model.*;
import nmka.util.CollectionUtils;
import nmka.util.ObjectMapper;
import nmka.util.ObjectUtils;
import nmka.util.StringUtils;

import java.util.*;

/**
 * Created by GYH on 2021/8/26
 */
public class DataInputHandler {
    private final int pageSize = 500;

    private void inDiameter(List<OcPipe> ocPipes, List<OcPump> ocPumps, List<OcValve> ocValves, InpDataMap dataMap) {
        Map<String, Double> diameters = dataMap.diameters;
        ocPipes.forEach(p -> diameters.put(p.getCode(), p.getDiameter()));
        ocValves.forEach(p -> diameters.put(p.getCode(), p.getDiameter()));
        ocPumps.forEach(p -> diameters.put(p.getCode(), p.getDiameter()));
    }

    public void inReport(InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.report;
        bufferedWriter.write("[REPORT]").newLine()
                .write("Status").newTab()
                .write("FULL").newLine()
                .write("Summary").newTab()
                .write("YES").newLine()
                .write("NODES").newTab()
                .write("ALL").newLine()
                .write("LINKS").newTab()
                .write("ALL")
                .newLine()
                .newLine();
    }

    public void inCoordinates(InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.coordinateBuff;
        bufferedWriter.write("[COORDINATES]").newLine();
        if (CollectionUtils.isNotEmpty(dataMap.nodeCoordinate)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("PointX").newTab()
                    .write("PointY").newLine();
            dataMap.nodeCoordinate.sort(Comparator.comparing(CodeNodeCoordinateResponse::getCode));
            for (CodeNodeCoordinateResponse coordinate : dataMap.nodeCoordinate) {
                bufferedWriter.write(coordinate.getCode()).newTab();
                bufferedWriter.writeDouble(coordinate.getLongitude()).newTab();
                bufferedWriter.writeDouble(coordinate.getLatitude()).newLine();
            }
        }
        bufferedWriter.newLine();
    }

    public void inStatus(InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.statusBuff;
        bufferedWriter.write("[STATUS]");
        bufferedWriter.newLine();
        if (CollectionUtils.isNotEmpty(dataMap.statusList)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("InitStatus").newLine();
            dataMap.statusList.sort(Comparator.comparing(Status::getCode));

            for (Status s : dataMap.statusList) {
                bufferedWriter.write(s.getCode()).newTab();
                if (StringUtils.isNotBlank(s.getInitStatus())) {
                    bufferedWriter.write(s.getInitStatus());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
    }

    public boolean inJunctions(List<OcPoint> ocPoints, InpDataMap dataMap) {
        List<CodeNodeCoordinateResponse> coordinatesList = ObjectMapper.clone(ocPoints, CodeNodeCoordinateResponse.class);
        StringBuilderWriter bufferedWriter = dataMap.junctions;
        bufferedWriter.write("[JUNCTIONS]").newLine();
        if (CollectionUtils.isNotEmpty(ocPoints)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("Elev").newTab()
                    .write("Demand").newTab()
                    .write("Pattern").newLine();

            for (OcPoint junction : ocPoints) {
                bufferedWriter.write(junction.getCode()).newTab();
                bufferedWriter.writeDouble(junction.getElev()).newTab();
                if (!ObjectUtils.isEmpty(junction.getBasicDemand())) {
                    bufferedWriter.writeDouble(junction.getBasicDemand()).newTab();
                }
                if (StringUtils.isNotBlank(junction.getTimePatternId())) {
                    bufferedWriter.write(junction.getTimePatternId()).newTab();
                }
                if (StringUtils.isNotBlank(junction.getDescribeContent())) {
                    bufferedWriter.write(";").write(junction.getDescribeContent());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        dataMap.addAllNodeCoordinate(coordinatesList);
        return true;
    }

    public boolean inReservoirs(List<OcReservoir> ocReservoirs, InpDataMap dataMap) {
        List<CodeNodeCoordinateResponse> coordinatesList = ObjectMapper.clone(ocReservoirs, CodeNodeCoordinateResponse.class);
        StringBuilderWriter bufferedWriter = dataMap.reservoirs;
        bufferedWriter.write("[RESERVOIRS]").newLine();
        if (CollectionUtils.isNotEmpty(ocReservoirs)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("Head").newTab()
                    .write("Pattern").newLine();

            for (OcReservoir reservoir : ocReservoirs) {
                bufferedWriter.write(reservoir.getCode()).newTab();
                bufferedWriter.writeDouble(reservoir.getHead()).newTab();
                if (StringUtils.isNotBlank(reservoir.getTimePatternId())) {
                    bufferedWriter.write(reservoir.getTimePatternId()).newTab();
                }
                if (!ObjectUtils.isEmpty(reservoir.getDescribeContent())) {
                    bufferedWriter.write(";").write(reservoir.getDescribeContent());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        dataMap.addAllNodeCoordinate(coordinatesList);
        return true;
    }

    public boolean inTanks(List<OcPool> ocPools, InpDataMap dataMap) {
        List<CodeNodeCoordinateResponse> coordinatesList = ObjectMapper.clone(ocPools, CodeNodeCoordinateResponse.class);
        StringBuilderWriter bufferedWriter = dataMap.tanks;
        bufferedWriter.write("[TANKS]").newLine();
        if (CollectionUtils.isNotEmpty(ocPools)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("Elev").newTab()
                    .write("InitLevel").newTab()
                    .write("MinLevel").newTab()
                    .write("MaxLevel").newTab()
                    .write("Diameter").newTab()
                    .write("MinVol").newTab()
                    .write("VolCurveId").newLine();

            for (OcPool tank : ocPools) {
                bufferedWriter.write(tank.getCode()).newTab();
                bufferedWriter.writeDouble(tank.getElev()).newTab();
                bufferedWriter.writeDouble(tank.getInitLevel()).newTab();
                bufferedWriter.writeDouble(tank.getMinLevel()).newTab();
                bufferedWriter.writeDouble(tank.getMaxLevel()).newTab();
                bufferedWriter.writeDouble(tank.getDiameter()).newTab();
                bufferedWriter.writeDouble(tank.getMinVol()).newTab();
                if (StringUtils.isNotBlank(tank.getVolCurveId())) {
                    bufferedWriter.write(tank.getVolCurveId()).newTab();
                }
                if (StringUtils.isNotBlank(tank.getDescribeContent())) {
                    bufferedWriter.write(";").write(tank.getDescribeContent());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();

        StringBuilderWriter mixingBuffer = dataMap.mixing;
        mixingBuffer.write("[MIXING]").newLine();
        if (CollectionUtils.isNotEmpty(ocPools)) {
            mixingBuffer.write(";")
                    .write("Code").newTab()
                    .write("MixtureMode").newTab()
                    .write("MixtureRatio").newLine();

            for (OcPool mixing : ocPools) {
                mixingBuffer.write(mixing.getCode()).newTab();
                mixingBuffer.write(mixing.getMixtureMode(), "MIXED").newTab();
                mixingBuffer.writeDouble(mixing.getMixtureRatio());
                mixingBuffer.newLine();
            }
        }
        mixingBuffer.newLine();
        dataMap.addAllNodeCoordinate(coordinatesList);
        return true;
    }

    public boolean inPipes(List<OcPipe> ocPipes, InpDataMap dataMap) {
        ocPipes.forEach(value -> {
            if (value.getInitStatus() == null) value.setInitStatus("OPEN");
        });
        List<Status> pipesStatusList = ObjectMapper.clone(ocPipes, Status.class);
        dataMap.statusList.addAll(pipesStatusList);
        StringBuilderWriter bufferedWriter = dataMap.pipes;
        bufferedWriter.write("[PIPES]").newLine();
        if (CollectionUtils.isNotEmpty(ocPipes)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("StartNodeCode").newTab()
                    .write("EndNodeCode").newTab()
                    .write("Length").newTab()
                    .write("Diameter").newTab()
                    .write("Roughness").newTab()
                    .write("MinorLoss").newTab()
                    .write("InitStatus").newLine();

            for (OcPipe pipe : ocPipes) {
                bufferedWriter.write(pipe.getCode()).newTab();
                if (StringUtils.isNotBlank(pipe.getStartNodeCode())) {
                    bufferedWriter.write(pipe.getStartNodeCode()).newTab();
                }
                if (StringUtils.isNotBlank(pipe.getEndNodeCode())) {
                    bufferedWriter.write(pipe.getEndNodeCode()).newTab();
                }
                bufferedWriter.writeDouble(pipe.getLength()).newTab();
                if (!ObjectUtils.isEmpty(pipe.getDiameter())) {
                    if (pipe.getDiameter() == 0) bufferedWriter.write("0.1").newTab();
                    else bufferedWriter.writeDouble(pipe.getDiameter()).newTab();
                } else bufferedWriter.writeDouble(0.1).newTab();
                if (!ObjectUtils.isEmpty(pipe.getRoughness())) {
                    if (pipe.getRoughness() == 0) {
                        bufferedWriter.write("100").newTab();
                    } else {
                        bufferedWriter.writeDouble(pipe.getRoughness()).newTab();
                    }
                } else bufferedWriter.writeDouble(100d).newTab();
                bufferedWriter.writeDouble(pipe.getMinorLoss()).newTab();
                if (ObjectUtils.isEmpty(pipe.getInitStatus())) {
                    bufferedWriter.write(pipe.getInitStatus());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        dataMap.addAllLinkCoordinate(ObjectMapper.clone(ocPipes, CodeLinkCoordinateResponse.class));
        return true;
    }

    public boolean inPumps(List<OcPump> ocPumps, InpDataMap dataMap) {
        ocPumps.forEach(value -> {
            if (value.getInitStatus() == null) value.setInitStatus("OPEN");
        });
        List<Status> pumpsStatusList = ObjectMapper.clone(ocPumps, Status.class);
        dataMap.statusList.addAll(pumpsStatusList);
        StringBuilderWriter bufferedWriter = dataMap.pumps;
        bufferedWriter.write("[PUMPS]").newLine();
        if (CollectionUtils.isNotEmpty(ocPumps)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("StartNodeCode").newTab()
                    .write("EndNodeCode").newTab()
                    .write("Pattern").newLine();

            for (OcPump pump : ocPumps) {
                bufferedWriter.write(pump.getCode()).newTab();
                if (!ObjectUtils.isEmpty(pump.getStartNodeCode())) {
                    bufferedWriter.write(String.valueOf(pump.getStartNodeCode())).newTab();
                }
                if (!ObjectUtils.isEmpty(pump.getEndNodeCode())) {
                    bufferedWriter.write(String.valueOf(pump.getEndNodeCode())).newTab();
                }
                if (StringUtils.isNotBlank(pump.getCurveId())) {
                    bufferedWriter.write("HEAD ").write(pump.getCurveId());
                }
                if (pump.getPower() != null && pump.getPower() != 0) {
                    bufferedWriter.write("  POWER ").writeDouble(pump.getPower());
                }
                if (pump.getSpeedRatio() != null && pump.getSpeedRatio() != 0) {
                    bufferedWriter.write("  SPEED ").writeDouble(pump.getSpeedRatio());
                }
                if (StringUtils.isNotBlank(pump.getTimePatternId())) {
                    bufferedWriter.write("  PATTERN ").write(pump.getTimePatternId());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        dataMap.addAllLinkCoordinate(ObjectMapper.clone(ocPumps, CodeLinkCoordinateResponse.class));
        return true;
    }

    public boolean inValves(List<OcValve> ocValves, InpDataMap dataMap) {
        ocValves.forEach(it -> {
            Status status = new Status();
            status.setCode(it.getCode());
            status.setInitStatus(it.getSteadyState());
            status.setSchemeId(it.getModelId());
            dataMap.statusList.add(status);
        });
        StringBuilderWriter bufferedWriter = dataMap.valves;
        bufferedWriter.write("[VALVES]").newLine();
        if (CollectionUtils.isNotEmpty(ocValves)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("Node1").newTab()
                    .write("Node2").newTab()
                    .write("Diameter").newTab()
                    .write("Type").newTab()
                    .write("Setting").newTab()
                    .write("MinorLoss").newLine();

            for (OcValve valve : ocValves) {
                bufferedWriter.write(valve.getCode()).newTab();
                if (!ObjectUtils.isEmpty(valve.getStartNodeCode())) {
                    bufferedWriter.write(valve.getStartNodeCode()).newTab();
                }
                if (!ObjectUtils.isEmpty(valve.getEndNodeCode())) {
                    bufferedWriter.write(valve.getEndNodeCode()).newTab();
                }
                bufferedWriter.writeDouble(valve.getDiameter()).newTab();
                if (!ObjectUtils.isEmpty(valve.getType())) {
                    bufferedWriter.write(valve.getType()).newTab();
                }
                bufferedWriter.writeDouble(valve.getSetting()).newTab();
                bufferedWriter.writeDouble(valve.getMinorLoss()).newTab();
                if (StringUtils.isNotBlank(valve.getDescribeContent())) {
                    bufferedWriter.write(";").write(valve.getDescribeContent());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        dataMap.addAllLinkCoordinate(ObjectMapper.clone(ocValves, CodeLinkCoordinateResponse.class));
        return true;
    }

    public boolean inCurves(List<OcCurve> ocCurves, InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.curves;
        bufferedWriter.write("[CURVES]").newLine();
        if (CollectionUtils.isNotEmpty(ocCurves)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("XValue").newTab()
                    .write("YValue").newLine();

            for (OcCurve it : ocCurves) {
                bufferedWriter.write(it.getCode()).newTab();
                bufferedWriter.writeDouble(it.getxValue()).newTab();
                bufferedWriter.writeDouble(it.getyValue());
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        return true;
    }

    public boolean inDemands(List<OcPoint> ocPoints, InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.demands;
        bufferedWriter.write("[DEMANDS]").newLine();
        if (CollectionUtils.isNotEmpty(ocPoints)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("BasicDemand").newTab()
                    .write("TimePatternId").newLine();

            for (OcPoint demand : ocPoints) {
                bufferedWriter.write(demand.getCode()).newTab();
                bufferedWriter.writeDouble(demand.getBasicDemand()).newTab();
                if (StringUtils.isNotBlank(demand.getTimePatternId())) {
                    bufferedWriter.write(demand.getTimePatternId());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        return true;
    }


    public boolean inPatterns(List<OcPattern> ocPatterns, InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.patterns;
        bufferedWriter.write("[PATTERNS]").newLine();
        if (CollectionUtils.isNotEmpty(ocPatterns)) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("Multipliers").newLine();

            for (OcPattern pattern : ocPatterns) {
                if (StringUtils.isNotBlank(pattern.getMultipliers())) {
                    int num = 0;
                    bufferedWriter.newLine();
                    String code = pattern.getCode();
                    String multipliers = pattern.getMultipliers();
                    String[] split = multipliers.split(" ");
                    for (String s : split) {
                        if (num == 0) {
                            bufferedWriter.write(code).newTab();
                        }
                        bufferedWriter.write(s).newTab();
                        num++;
                        if (num > 5) {
                            bufferedWriter.newLine();
                            num = 0;
                        }
                    }
                }
            }
        }
        bufferedWriter.newLine();
        return true;
    }

    public boolean inQuality(List<OcPoint> ocPoints, List<OcPool> ocPools, List<OcReservoir> ocReservoirs, InpDataMap dataMap) {
        HashMap<String, Double> collect = ocPoints.stream().collect(HashMap::new, (h, c) -> h.put(c.getCode(), c.getInitialQuality()), HashMap::putAll);
        ocPools.forEach(c -> collect.put(c.getCode(), c.getInitialQuality()));
        ocReservoirs.forEach(c -> collect.put(c.getCode(), c.getInitialQuality()));
        StringBuilderWriter bufferedWriter = dataMap.source;
        bufferedWriter.write("[QUALITY]").newLine();
        Double d = 0.0;
        if (collect.size() > 0) {
            bufferedWriter.write(";")
                    .write("Code").newTab()
                    .write("InitialQuality").newLine();
            for (Map.Entry<String, Double> entry : collect.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().equals(d)) {
                    bufferedWriter.write(entry.getKey()).newTab()
                            .writeDouble(entry.getValue())
                            .newLine();
                }
            }
        }
        bufferedWriter.newLine();
        return true;
    }

    public boolean inSource(List<OcSource> ocSources, InpDataMap dataMap) {
        StringBuilderWriter bufferedWriter = dataMap.source;
        bufferedWriter.write("[SOURCES]").newLine();
        if (CollectionUtils.isNotEmpty(ocSources)) {
            bufferedWriter.write(";")
                    .write("NodeCode").newTab()
                    .write("Type").newTab()
                    .write("Strength").newTab()
                    .write("TimePatternId").newLine();

            for (OcSource sources : ocSources) {
                bufferedWriter.write(sources.getNodeCode()).newTab();
                bufferedWriter.write(sources.getType(), "CONCEN").newTab();
                bufferedWriter.writeDouble(sources.getStrength()).newTab();
                if (StringUtils.isNotBlank(sources.getTimePatternId())) {
                    bufferedWriter.write(sources.getTimePatternId());
                }
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.newLine();
        return true;
    }

}
