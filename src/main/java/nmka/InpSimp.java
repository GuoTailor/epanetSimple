package nmka;

import nmka.model.*;
import nmka.epanet.network.Network;
import nmka.epanet.network.io.input.InputParser;
import nmka.epanet.network.structures.*;
import nmka.epanet.network.structures.Link;
import nmka.epanet.network.structures.Node;
import nmka.epanet.network.structures.Source;
import nmka.handler.CreateInpHandler;
import nmka.handler.DataInputHandler;
import nmka.handler.InpDataMap;
import nmka.simplify.NetworkDataInfo;
import nmka.simplify.SimpleNetwork;
import nmka.test.CodeGenerate1;
import nmka.tool.MapInfo;
import nmka.tool.SubNetworkInfo;
import nmka.tool.SubNetworkSeek;
import nmka.util.CollectionUtils;
import nmka.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class InpSimp {
    Logger log = Logger.getLogger(InpSimp.class.toString());

    public static void main(String[] args) {
        System.out.println("输入inp路径");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        System.out.println("输入要筛选管道的直径");
        new InpSimp().inpInput(path, scanner.nextInt());
        System.out.println("完成，按任意建结束");
        scanner.nextLine();
        scanner.nextLine();
        System.exit(0);
    }

    public void inpInput(String path, int size) {
        ThreadManager.init(32);
        Network net = new Network();
        File inFile = new File(path);
        if (!inFile.exists()) {
            log.info("File not found !");
            return;
        }
        try {
            InputParser parserINP = InputParser.create(log);
            parserINP.parse(net, inFile);
            Set<OcSource> ocSources = new HashSet<>();
            Set<OcPattern> ocPatterns = new HashSet<>();
            List<OcPoint> ocPoints = new LinkedList<>();
            Set<OcCurve> ocCurves = new HashSet<>();
            List<OcPool> ocPools = new LinkedList<>();
            List<OcReservoir> ocReservoirs = new LinkedList<>();
            List<OcPipe> ocPipes = new LinkedList<>();
            List<OcPump> ocPumps = new LinkedList<>();
            List<OcValve> ocValves = new LinkedList<>();
            net.getPatterns().forEach(pattern -> ocPatterns.add(patternToOcPattern(pattern)));
            net.getNodes().forEach(node -> {
                node.getDemand().forEach(demand -> ocPatterns.add(patternToOcPattern(demand.getPattern())));
                OcSource ocSource = null;
                if (node.getSource() != null) {
                    ocPatterns.add(patternToOcPattern(node.getSource().getPattern()));
                    ocSource = sourceToOcSource(node.getSource(), node.getId());
                    ocSources.add(ocSource);
                }
                Long sourceId = null;
                if (ocSource != null) {
                    sourceId = ocSource.getId();
                }
                if (node instanceof Tank) {
                    ocPools.add(tankToOcPool((Tank) node, sourceId));
                } else if (node instanceof Reservoir) {
                    ocReservoirs.add(reservoirToOcReservoir((Reservoir) node, sourceId));
                } else {
                    ocPoints.add(nodeToOcPoint(node, sourceId));
                }
            });
            net.getCurves().forEach(curve -> ocCurves.add(curveToOcCurve(curve)));
            net.getLinks().forEach(link -> {
                if (link instanceof Pump) {
                    ocPumps.add(pumpToOcPump((Pump) link));
                } else if (link instanceof Valve) {
                    ocValves.add(valveToOcValve((Valve) link));
                } else {
                    ocPipes.add(linkToOcPipe(link));
                }
            });
            int maxId = findMaxId(ocPoints, ocPools, ocReservoirs, ocPipes, ocPumps, ocValves);
            CodeGenerate1.setInt(maxId);
            log.info("END_RUN_OK");
            removeif(ocPipes, ocPumps, ocValves, size);
            log.info("point simp ok");
            NetworkDataInfo dataInfo = new NetworkDataInfo(ocPoints, ocReservoirs, ocPools, ocPipes, ocPumps, ocValves);
            SubNetworkSeek networkSeek = new SubNetworkSeek();
            SubNetworkInfo subNetworkInfo = networkSeek.find(dataInfo);
            subNetworkInfo.badLink.forEach(l -> deletePipe(l.code, dataInfo));
            deleteSubNetwork(subNetworkInfo.subNetwork, dataInfo);

            log.info("开始简化");
            SimpleNetwork simpleNetwork = new SimpleNetwork();
            simpleNetwork.simplify(dataInfo);
            log.info("简化完成");
            InpDataMap dataMap = new InpDataMap();
            ThreadManager threadManager = ThreadManager.getInstance();
            DataInputHandler inputHandler = new DataInputHandler();
            List<Future<Boolean>> futures = new ArrayList<>(16);
            futures.add(threadManager.submit(() -> inputHandler.inJunctions(ocPoints, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inReservoirs(ocReservoirs, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inTanks(ocPools, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inPipes(ocPipes, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inPumps(ocPumps, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inValves(ocValves, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inCurves(new ArrayList<>(ocCurves), dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inDemands(ocPoints, dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inPatterns(new ArrayList<>(ocPatterns), dataMap)));
            futures.add(threadManager.submit(() -> inputHandler.inQuality(ocPoints, ocPools, ocReservoirs, dataMap)));
            for (Future<Boolean> future : futures) {
                future.get();
            }
            inputHandler.inCoordinates(dataMap);
            inputHandler.inStatus(dataMap);
            inputHandler.inReport(dataMap);
            CreateInpHandler createInpHandler = new CreateInpHandler();
            String filePath = createInpHandler.write(dataMap);
            System.out.println(filePath);
        } catch (Exception e) {
            log.info("END_RUN_ERR");
            e.printStackTrace();
        }
    }

    public void deletePipe(String code, NetworkDataInfo dataInfo) {
        dataInfo.pipes.removeIf(p -> p.getCode().equals(code));
        dataInfo.pumps.removeIf(p -> p.getCode().equals(code));
        dataInfo.valves.removeIf(p -> p.getCode().equals(code));
    }

    public void deletePoint(String code, NetworkDataInfo dataInfo) {
        dataInfo.points.removeIf(p -> p.getCode().equals(code));
        dataInfo.pools.removeIf(p -> p.getCode().equals(code));
        dataInfo.reservoirs.removeIf(p -> p.getCode().equals(code));
    }

    public int findMaxId(List<OcPoint> ocPoints,
                          List<OcPool> ocPools,
                          List<OcReservoir> ocReservoirs,
                          List<OcPipe> ocPipes,
                          List<OcPump> ocPumps,
                          List<OcValve> ocValves) {
        int max = 0;
        /*if (!ocPoints.isEmpty())
            max = Math.max(ocPoints.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
        if (!ocPools.isEmpty())
            max = Math.max(ocPools.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
        if (!ocReservoirs.isEmpty())
            max = Math.max(ocReservoirs.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
        if (!ocPipes.isEmpty())
            max = Math.max(ocPipes.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
        if (!ocPumps.isEmpty())
            max = Math.max(ocPumps.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
        if (!ocValves.isEmpty())
            max = Math.max(ocValves.stream().filter(p -> !p.getCode().startsWith("OC")).mapToInt(p -> Integer.parseInt(p.getCode())).max().orElse(0), max);
       */ return max;
    }

    public void deleteSubNetwork(List<MapInfo> subNetwork, NetworkDataInfo dataInfo) {
        int max = 0;
        MapInfo main = null;
        for (MapInfo mapInfo : subNetwork) {
            boolean poolOrReservoir = isNotRemove(mapInfo);
            if (poolOrReservoir) mapInfo.pools++;
            int size = mapInfo.nodes.size();
            if (size > max) {
                max = size;
                main = mapInfo;
            }
        }
        for (MapInfo mapInfo : subNetwork) {
            if (main != mapInfo && mapInfo.pools == 0) {
                mapInfo.nodes.forEach(n -> deletePoint(n.code, dataInfo));
                mapInfo.links.forEach(l -> deletePipe(l.code, dataInfo));
            }
        }
    }

    public boolean isNotRemove(MapInfo mapInfo) {
        if (mapInfo != null && CollectionUtils.isNotEmpty(mapInfo.nodes)) {
            for (nmka.tool.Node node : mapInfo.nodes) {
                if (node.target instanceof OcPool || node.target instanceof OcReservoir) return true;
                if (node.target instanceof OcPoint && ((OcPoint) node.target).getDescribeContent().contains("进水点")) return true;
            }
        }
        return false;
    }

    public void removeif(List<OcPipe> ocPipes, List<OcPump> ocPumps, List<OcValve> ocValves, double size) {
        ocPipes.removeIf(p -> p.getDiameter() < size);
        ocPumps.removeIf(p -> p.getDiameter() < size);
        ocValves.removeIf(p -> p.getDiameter() < size);
    }

    public boolean getNode(String code, List<OcPipe> ocPipes, List<OcPump> ocPumps, List<OcValve> ocValves) {
        OcPipe ocPipe = ocPipes.stream()
                .filter(pipe -> code.equals(pipe.getStartNodeCode()) || code.equals(pipe.getEndNodeCode()))
                .findFirst()
                .orElse(null);
        if (ocPipe != null) return true;
        OcPump ocPump = ocPumps.stream()
                .filter(pump -> pump.getStartNodeCode().equals(code) || pump.getEndNodeCode().equals(code))
                .findFirst()
                .orElse(null);
        if (ocPump != null) return true;
        OcValve ocValve = ocValves.stream()
                .filter(valve -> valve.getStartNodeCode().equals(code) || valve.getEndNodeCode().equals(code))
                .findFirst()
                .orElse(null);
        return ocValve != null;
    }

    public OcValve valveToOcValve(Valve valve) {
        OcValve ocValve = new OcValve();
        ocValve.setFlag(1);
        ocValve.setCreatorId(100L);
        ocValve.setModelId(1L);
        ocValve.setNetworkId(1L);
        ocValve.setCode(valve.getId());
        ocValve.setStartNodeCode(valve.getFirst().getId());
        ocValve.setStartLatitude(valve.getFirst().getPosition().getY());
        ocValve.setStartLongitude(valve.getFirst().getPosition().getX());
        ocValve.setEndNodeCode(valve.getSecond().getId());
        ocValve.setStartLatitude(valve.getSecond().getPosition().getY());
        ocValve.setStartLongitude(valve.getSecond().getPosition().getX());

        ocValve.setDescribeContent(valve.getComment());
        ocValve.setDiameter(valve.getDiameter());
        ocValve.setType(valve.getType().parseStr);
        ocValve.setSetting(valve.getRoughness());
        ocValve.setMinorLoss(valve.getKm());
        ocValve.setSteadyState(valve.getStat().parseStr);
        return ocValve;
    }

    public OcPump pumpToOcPump(Pump pump) {
        OcPump ocPump = new OcPump();
        ocPump.setFlag(1);
        ocPump.setCreatorId(100L);
        ocPump.setModelId(1L);
        ocPump.setNetworkId(1L);
        ocPump.setCode(pump.getId());
        ocPump.setStartNodeCode(pump.getFirst().getId());
        ocPump.setStartLatitude(pump.getFirst().getPosition().getY());
        ocPump.setStartLongitude(pump.getFirst().getPosition().getX());

        ocPump.setEndNodeCode(pump.getSecond().getId());
        ocPump.setEndLatitude(pump.getSecond().getPosition().getY());
        ocPump.setEndLongitude(pump.getSecond().getPosition().getX());

        ocPump.setDescribeContent(pump.getComment());
        ocPump.setCurveId(pump.getHcurve().getId());
        ocPump.setPower(pump.getKm());
        ocPump.setSpeedRatio(pump.getRoughness());
        ocPump.setInitStatus(pump.getStat().parseStr);
        Curve ecurve = pump.getEcurve();
        if (ecurve != null) {
            ocPump.setEfficiencyCurveId(ecurve.getId());
        }
        ocPump.setEnergyPrices(pump.getEcost());
        if (pump.getEpat() != null)
            ocPump.setPricesPatternId(pump.getEpat().getId());
        return ocPump;
    }

    public OcPipe linkToOcPipe(Link link) {
        OcPipe pipe = new OcPipe();
        pipe.setFlag(1);
        pipe.setCreatorId(100L);
        pipe.setModelId(1L);
        pipe.setNetworkId(1L);
        pipe.setCode(link.getId());
        if (link.getFirst() != null) {
            pipe.setStartElev(link.getFirst().getElevation());
            pipe.setStartNodeCode(link.getFirst().getId());
            pipe.setStartLatitude(link.getFirst().getPosition().getY());
            pipe.setStartLongitude(link.getFirst().getPosition().getX());
        }
        if (link.getSecond() != null) {
            pipe.setEndElev(link.getSecond().getElevation());
            pipe.setEndNodeCode(link.getSecond().getId());
            pipe.setEndLatitude(link.getSecond().getPosition().getY());
            pipe.setEndLongitude(link.getSecond().getPosition().getX());
        }
        pipe.setDescribeContent(link.getComment());
        pipe.setLength(link.getLenght());
        pipe.setDiameter(link.getDiameter());
        pipe.setRoughness(link.getRoughness());
        pipe.setMinorLoss(link.getKm());
        pipe.setInitStatus(link.getStat().parseStr);
        pipe.setMainstream(link.getKb());
        pipe.setTubeWall(link.getKw());
        return pipe;
    }

    public OcCurve curveToOcCurve(Curve curve) {
        OcCurve ocCurve = new OcCurve();
        ocCurve.setFlag(1);
        ocCurve.setModelId(1L);
        ocCurve.setCreatorId(100L);
        ocCurve.setCode(curve.getId());
        Integer type = null;
        if (curve.getType() != null) {
            switch (curve.getType()) {
                case E_CURVE:
                    type = 3;
                    break;
                case H_CURVE:
                    type = 1;
                    break;
                case P_CURVE:
                    type = 2;
                    break;
                case V_CURVE:
                    type = 4;
                    break;
                default:
                    type = null;
            }
        }
        ocCurve.setType(type);
        ocCurve.setxValue(curve.getX().get(0));
        ocCurve.setyValue(curve.getY().get(0));
        return ocCurve;
    }

    public OcPool tankToOcPool(Tank tank, Long sourceId) {
        OcPool pool = new OcPool();
        pool.setFlag(1);
        pool.setCreatorId(100L);
        pool.setCode(tank.getId());
        pool.setModelId(1L);
        pool.setNetworkId(1L);
        pool.setPointX(tank.getPosition().getX());
        pool.setPointY(tank.getPosition().getY());
        pool.setLatitude(tank.getPosition().getY());
        pool.setLongitude(tank.getPosition().getX());
        pool.setElev(tank.getElevation());
        pool.setDescribeContent(tank.getComment());
        pool.setInitialQuality(tank.getC0()[0]);
        pool.setSourceQuality(sourceId);
        pool.setInitLevel(tank.getH0());
        pool.setMinLevel(tank.getHmin());
        pool.setMaxLevel(tank.getHmax());
        pool.setDiameter(tank.getArea());
        pool.setMinVol(tank.getVmin());
        if (tank.getVcurve() != null) {
            pool.setVolCurveId(tank.getVcurve().getId());
        }
        pool.setMixtureMode(tank.getMixModel().parseStr);
        pool.setMixtureRatio(tank.getV1max());
        pool.setReaction(tank.getKb());
        return pool;
    }

    public OcReservoir reservoirToOcReservoir(Reservoir tank, Long sourceId) {
        OcReservoir reservoir = new OcReservoir();
        reservoir.setFlag(1);
        reservoir.setCreatorId(100L);
        reservoir.setCode(tank.getId());
        reservoir.setModelId(1L);
        reservoir.setNetworkId(1L);
        reservoir.setPointX(tank.getPosition().getX());
        reservoir.setPointY(tank.getPosition().getY());
        reservoir.setLatitude(tank.getPosition().getY());
        reservoir.setLongitude(tank.getPosition().getX());
        reservoir.setHead(tank.getElevation());
        reservoir.setDescribeContent(tank.getComment());
        reservoir.setInitialQuality(tank.getC0()[0]);
        reservoir.setSourceQualityId(sourceId);
        if (tank.getPattern() != null)
            reservoir.setTimePatternId(tank.getPattern().getId());
        return reservoir;
    }

    public OcSource sourceToOcSource(Source source, String nodeId) {
        OcSource ocSource = new OcSource();
        ocSource.setFlag(1);
        ocSource.setModelId(1L);
        ocSource.setCreatorId(100L);
        ocSource.setNodeCode(nodeId);
        ocSource.setType(source.getType().parseStr);
        ocSource.setStrength(source.getC0());
        ocSource.setTimePatternId(source.getPattern().getId());
        return ocSource;
    }

    public OcPoint nodeToOcPoint(Node node, Long sourceId) {
        OcPoint point = new OcPoint();
        point.setFlag(1);
        point.setCreatorId(100L);
        point.setModelId(1L);
        point.setNetworkId(1L);
        point.setCode(node.getId());
        point.setPointX(node.getPosition().getX());
        point.setPointY(node.getPosition().getY());
        point.setLatitude(node.getPosition().getY());
        point.setLongitude(node.getPosition().getX());
        point.setElev(node.getElevation());
        point.setDescribeContent(node.getComment());
        point.setBasicDemand(node.getDemand().get(0).getBase());
        point.setTimePatternId(node.getDemand().get(0).getPattern().getId());
        point.setCoefficient(node.getKe());
        point.setInitialQuality(node.getC0()[0]);
        point.setSourceQualityId(sourceId);
        return point;
    }

    public OcPattern patternToOcPattern(Pattern pattern) {
        OcPattern ocPattern = new OcPattern();
        ocPattern.setModelId(1L);
        ocPattern.setFlag(1);
        ocPattern.setCreatorId(100L);
        StringBuilder sb = new StringBuilder();
        pattern.getFactorsList().forEach(s -> sb.append(s).append(" "));
        ocPattern.setMultipliers(sb.toString());
        String code = StringUtils.isEmpty(pattern.getId()) ? "null" : pattern.getId();
        ocPattern.setCode(code);
        return ocPattern;
    }
}
