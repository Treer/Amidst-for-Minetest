package amidst.minetest.world.mapgen;

import java.util.Arrays;

import amidst.logging.AmidstLogger;

/**
 * MGVALLEYS_HUMID_RIVERS effect applied to Minetest's default climate
 * Specifically the humidity histogram is multiplied by a histogram of
 *   1 + pow(0.5, max(water_depth, 1))
 * where water_depth is a value of 0 or more, and is the distribution of
 * water_depth is determined by the valleys terrain mapgen. 
 */
public class ClimateHistogram_ValleysHumidRivers extends ClimateHistogram {

	private static final boolean RECALCULATE_HISTOGRAM = false;
	
	class HistogramData {	
		long[] samples;
		double samplesCount;
		int    sampleOffset;
		double mean;
		double median;
		double[] frequencyAtPercentileTable;
		double[] frequencyAtPerdimileTable;
		
		public HistogramData(long[] samples, double samplesCount, int sampleOffset, double mean, double[] frequencyAtPercentileTable, double[] frequencyAtPerdimileTable) {
			this.samples                    = samples;
			this.samplesCount               = samplesCount;
			this.sampleOffset               = sampleOffset;
			this.mean                       = mean;
			this.frequencyAtPercentileTable = frequencyAtPercentileTable;
			this.frequencyAtPerdimileTable  = frequencyAtPerdimileTable;			
		}
	};	
	
	float  sampledHumidityScale_rangeMin = 1.0f; 
	float  sampledHumidityScale_rangeMax = 1.5f; 
	// represents a value range from 1.0 to 1.5 (sampledHumidityScale_rangeMin to sampledHumidityScale_rangeMax), i.e [0] = 1, [101] = 1.5
	long[] sampledHumidityScale_invarientRiverDepth = new long[] {226928918, 101229921, 59380168, 44424690, 36193937, 30772819, 27024116, 24215483, 21939141, 20146323, 18648897, 17445982, 16463378, 15554719, 14790947, 14098957, 13419908, 12865735, 12409313, 11951780, 11518433, 11145477, 10803420, 10478076, 10150488, 9881886, 9629398, 9370933, 9112072, 8910496, 8719855, 8525298, 8354557, 8171182, 8008397, 7850265, 7701778, 7559718, 7452647, 7305844, 7164413, 7039670, 6930638, 6814618, 6722468, 6612824, 6505793, 6399769, 6299887, 6221978, 6152176, 6073317, 6011843, 5940158, 5851840, 5780081, 5705484, 5635537, 5588398, 5531554, 5466569, 5408179, 5345162, 5287230, 5234976, 5191892, 5155196, 5093573, 5034905, 4999841, 4949875, 4916582, 4863620, 4821304, 4778019, 4753338, 4699673, 4667702, 4625900, 4591079, 4552494, 4518390, 4483879, 4451114, 4417539, 4379471, 4347587, 4319516, 4285507, 4261799, 4232316, 4194433, 4175905, 4145593, 4109063, 4082616, 4054033, 4032761, 4010350, 3986994, 3963920, 3938671, 3922770, 3902474, 3869988, 3850814, 3824743, 3806204, 3773523, 3751777, 3733315, 3712530, 3695061, 3670488, 3652072, 3632389, 3613120, 3596819, 3580708, 3573270, 3553977, 3539561, 3528992, 3496232, 3477196, 3459416, 3438422, 3417214, 3411635, 3403189, 3382710, 3368238, 3350478, 3333616, 3316991, 3307474, 3295966, 3275484, 3266876, 3249823, 3239660, 3222848, 3212404, 3199131, 3183829, 3168635, 3162041, 3152528, 3146399, 3130517, 3119207, 3111260, 3102235, 3091003, 3076158, 3064381, 3049831, 3035936, 3026706, 3014732, 3004473, 2997861, 2984506, 2974798, 2967298, 2958706, 2958985, 2937203, 2927179, 2913595, 2904399, 2892855, 2888141, 2876845, 2863260, 2852675, 2841341, 2837802, 2827594, 2819141, 2814355, 2807403, 2798354, 2787924, 2776672, 2766648, 2763549, 2760466, 2751212, 2741578, 2738075, 2737934, 2727764, 2722540, 2719119, 2702937, 2690034, 2685855, 2679645, 2665729, 2664655, 2651579, 2643758, 2633403, 2625448, 2625205, 2623465, 2607894, 2600663, 2596970, 2585907, 2576946, 2572252, 2569437, 2563786, 2560116, 2551268, 2540240, 2535948, 2525405, 2521516, 2516029, 2508722, 2503943, 2500502, 2488710, 2483726, 2480671, 2475688, 2473383, 2471243, 2465639, 2460773, 2456022, 2447856, 2447009, 2439341, 2430733, 2430852, 2421952, 2416651, 2412679, 2408949, 2404853, 2401039, 2393203, 2386039, 2381154, 2378836, 2377300, 2371103, 2365990, 2359485, 2355238, 2353745, 2354589, 2347952, 2349410, 2343384, 2336510, 2329689, 2337177, 2329011, 2328612, 2322447, 2318488, 2309862, 2307500, 2308702, 2306822, 2292575, 2289442, 2286648, 2282205, 2279991, 2275584, 2275071, 2273232, 2268093, 2260617, 2254704, 2251983, 2249493, 2249573, 2251271, 2242936, 2241577, 2242705, 2240883, 2232301, 2230224, 2225633, 2220550, 2223692, 2213282, 2209228, 2210603, 2211927, 2208317, 2202645, 2198243, 2196019, 2191433, 2187690, 2185004, 2184974, 2180252, 2181488, 2172268, 2170628, 2165212, 2161312, 2165250, 2159379, 2156829, 2151950, 2148175, 2147719, 2144300, 2142025, 2134436, 2135249, 2132345, 2127871, 2133474, 2127776, 2124120, 2120145, 2117514, 2114463, 2114309, 2104911, 2108161, 2101914, 2099095, 2095797, 2091346, 2090781, 2089373, 2085327, 2083480, 2080072, 2075559, 2074463, 2068608, 2064041, 2063951, 2070985, 2059476, 2057342, 2057139, 2050611, 2048655, 2045057, 2046274, 2045459, 2044499, 2044072, 2040678, 2034762, 2034010, 2030626, 2031700, 2027063, 2027980, 2023047, 2022512, 2021952, 2018779, 2014665, 2010157, 2010943, 2007637, 2008581, 2003233, 2006261, 2010200, 2006638, 2004666, 2001386, 1997174, 1998498, 1993897, 1993093, 1992812, 1994409, 1990688, 1987072, 1989053, 1989007, 1988844, 1987263, 1984349, 1983776, 1977788, 1977110, 1978915, 1976323, 1974289, 1969890, 1969032, 1967535, 1966437, 1964626, 1963767, 1963570, 1961708, 1957281, 1961325, 1962137, 1959344, 1959341, 1962603, 1955870, 1951862, 1948865, 1949368, 1949138, 1949828, 1955664, 1944988, 1941377, 1942003, 1939192, 1938820, 1937623, 1942207, 1941138, 1937216, 1934406, 1935377, 1934238, 1932947, 1929889, 1935140, 1931001, 1929275, 1927265, 1929585, 1927031, 1926016, 1923416, 1917215, 1916683, 1914647, 1915753, 1913921, 1913519, 1911891, 1914733, 1913036, 1914373, 1911454, 1912854, 1912727, 1913288, 1919496, 1913678, 1908033, 1910998, 1908981, 1909646, 1901257, 1904810, 1900141, 1902522, 1900535, 1902607, 1901206, 1899066, 1898635, 1898854, 1904407, 1899765, 1894850, 1894180, 1895431, 1896801, 1893717, 1889643, 1890530, 1886436, 1887822, 1888151, 1884865, 1885940, 1882056, 1883982, 1881478, 1886153, 1881105, 1880642, 1878510, 1879839, 1877935, 1876610, 1882073, 1880444, 1880577, 1878948, 1637033126};
	// represents a value range from 1.0 to 1.5 (sampledHumidityScale_rangeMin to sampledHumidityScale_rangeMax), i.e [0] = 1, [101] = 1.5
	long[] sampledHumidityScale_varientRiverDepth   = new long[] {247533515, 109533270, 64708819, 48391495, 39204116, 33189998, 29156128, 26188148, 23851678, 21946009, 20380625, 19016357, 17963516, 16942389, 16156722, 15392180, 14686201, 14078570, 13555454, 13107988, 12650541, 12219150, 11837650, 11473119, 11119939, 10827930, 10549641, 10287866, 10007208, 9775739, 9555452, 9344633, 9164919, 8994265, 8822875, 8655086, 8488155, 8322749, 8189674, 8044110, 7908287, 7787711, 7681555, 7568917, 7459387, 7358121, 7241400, 7129109, 7042961, 6960279, 6872072, 6794136, 6713799, 6630535, 6547259, 6467867, 6396366, 6334516, 6270094, 6205343, 6132619, 6072196, 6005900, 5942319, 5883802, 5828653, 5774016, 5718429, 5665052, 5622271, 5573833, 5537409, 5486573, 5450732, 5409827, 5378754, 5332163, 5288860, 5251708, 5215483, 5184102, 5140157, 5104039, 5067592, 5028673, 4984583, 4949850, 4917024, 4881836, 4852521, 4820115, 4783870, 4757293, 4726985, 4700521, 4667786, 4642730, 4621331, 4594190, 4569147, 4547260, 4521042, 4498950, 4479743, 4449465, 4422835, 4400627, 4383781, 4355607, 4331950, 4312002, 4292692, 4266528, 4247745, 4227221, 4208030, 4191524, 4173448, 4154594, 4139344, 4125873, 4106272, 4087448, 4067425, 4053825, 4033308, 4017621, 3997740, 3986709, 3967907, 3953207, 3941003, 3925971, 3912197, 3894413, 3884778, 3872679, 3854411, 3837584, 3829217, 3817660, 3805175, 3796482, 3779995, 3768541, 3751364, 3743896, 3728378, 3717126, 3710230, 3696269, 3686711, 3679336, 3666356, 3652829, 3638206, 3628232, 3611982, 3597994, 3585082, 3575908, 3567925, 3556317, 3546353, 3544190, 3533282, 3528921, 3508151, 3495586, 3488178, 3479882, 3465651, 3459115, 3445426, 3436433, 3429772, 3414781, 3407219, 3395806, 3387558, 3379591, 3366701, 3360705, 3347956, 3340050, 3330718, 3323952, 3316667, 3307238, 3297094, 3294009, 3292411, 3283586, 3283350, 3274101, 3262078, 3251634, 3248330, 3242199, 3229828, 3225668, 3210969, 3206500, 3200613, 3190430, 3187402, 3179328, 3171871, 3165333, 3159623, 3150424, 3144968, 3138034, 3140634, 3130559, 3126486, 3120515, 3115292, 3109754, 3104591, 3097812, 3093680, 3088951, 3079938, 3071830, 3066722, 3059968, 3058760, 3047492, 3043597, 3038779, 3031519, 3029606, 3025100, 3018504, 3015247, 3009028, 3006344, 3001477, 2996286, 2994645, 2988453, 2987361, 2979062, 2976987, 2970924, 2966363, 2960447, 2960790, 2955170, 2953987, 2947137, 2945364, 2941399, 2935333, 2932425, 2930533, 2925502, 2925862, 2919897, 2915129, 2924186, 2916245, 2907797, 2908526, 2898378, 2898899, 2892125, 2893105, 2887397, 2882379, 2881008, 2874482, 2871675, 2867949, 2863243, 2861810, 2859903, 2856763, 2855690, 2851203, 2847908, 2850924, 2850494, 2848965, 2843718, 2838359, 2835937, 2828178, 2828862, 2825286, 2822094, 2817265, 2816321, 2810505, 2805545, 2804307, 2801773, 2799466, 2795244, 2792338, 2792977, 2785779, 2785103, 2779986, 2777108, 2775461, 2773934, 2771388, 2770282, 2768688, 2763687, 2763258, 2755586, 2752949, 2750090, 2749733, 2744246, 2743350, 2743501, 2734447, 2735034, 2731756, 2729115, 2726588, 2722016, 2718421, 2715730, 2710867, 2708265, 2711530, 2706592, 2706057, 2704728, 2697659, 2699568, 2695069, 2695815, 2693882, 2687579, 2684036, 2682005, 2681462, 2677340, 2676673, 2675398, 2673814, 2674145, 2668472, 2667324, 2670382, 2668257, 2667365, 2658693, 2663034, 2665626, 2661721, 2662031, 2658900, 2656815, 2652366, 2652887, 2649522, 2649211, 2650284, 2644497, 2644359, 2641588, 2642000, 2640123, 2638455, 2640096, 2637945, 2635476, 2635305, 2638252, 2636045, 2633784, 2631729, 2631046, 2630545, 2626660, 2628257, 2629727, 2629177, 2628680, 2626482, 2624560, 2621137, 2625363, 2625305, 2621685, 2621076, 2622400, 2617965, 2620469, 2617347, 2615415, 2613852, 2614746, 2612548, 2611643, 2610733, 2606339, 2609683, 2609210, 2610035, 2609199, 2607953, 2608865, 2610548, 2612050, 2613246, 2610742, 2609705, 2610455, 2613594, 2610759, 2609744, 2615258, 2610879, 2609763, 2610514, 2610502, 2608657, 2613071, 2614090, 2612342, 2613654, 2614296, 2614605, 2617043, 2617692, 2613238, 2618688, 2616554, 2617934, 2618448, 2617079, 2617599, 2618062, 2617781, 2614365, 2616499, 2617210, 2619300, 2618462, 2615693, 2616210, 2620365, 2619799, 2622476, 2622378, 2625414, 2621632, 2624697, 2625427, 2627064, 2626540, 2630317, 2630128, 2632632, 2631058, 2634322, 2634116, 2632729, 2632395, 2635599, 2630913, 2633377, 2635083, 2637956, 2639760, 2634981, 2635336, 2638189, 2643301, 2644223, 2641170, 2645323, 2647625, 2646966, 2651174, 2651575, 2652072, 2651164, 2651502, 2651441, 2651068, 2661082, 2658812, 2659126, 2659562, 2662439, 2661043, 2661887, 2665738, 2668626, 2668238, 2668807, 1267661935};   	

	/** Pre-calculated data, see RECALCULATE_HISTOGRAM and code in the constructor if you wish to recalculate */
	HistogramData humidRiversHistogram_varientRiverDepth = new HistogramData(
		new long[] {0, 33, 220, 599, 1919, 4053, 7141, 11774, 17779, 25374, 40345, 63005, 94726, 147731, 211932, 288554, 388854, 512976, 664661, 859088, 1083424, 1341413, 1676924, 2074307, 2542445, 3130822, 3812355, 4603173, 5589940, 6733780, 8048981, 9602605, 11360955, 13339074, 15618333, 18196706, 21109718, 24543677, 28399190, 32725444, 37603462, 42958741, 48822433, 55343184, 62495556, 70354074, 78925179, 88091735, 97895478, 108517324, 119833072, 131797075, 144614354, 158267485, 172801822, 188307957, 204656968, 221719684, 239546087, 258046116, 277358294, 297947305, 319852558, 342841892, 366470714, 390376171, 414728064, 439760872, 465264214, 491173083, 517688235, 544561915, 571722748, 599065473, 626415364, 653647879, 680968626, 708438915, 736088610, 763742770, 791159879, 818265109, 844944247, 871256876, 897116790, 922651453, 947739220, 972405658, 996628672, 1020256540, 1043176812, 1065447543, 1087176473, 1108055092, 1127719573, 1146664106, 1165121433, 1183062444, 1200432222, 1216945010, 1232470751, 1247166674, 1261185499, 1274374895, 1286816588, 1298502068, 1309412672, 1319691950, 1329452446, 1338162710, 1345894976, 1352711800, 1358769231, 1364179147, 1369116955, 1373555756, 1377242078, 1380173853, 1382026415, 1383032454, 1383286778, 1382615887, 1381110810, 1378659905, 1375241382, 1371216480, 1366724814, 1361625384, 1355640185, 1348676500, 1340974633, 1332733490, 1323832597, 1314082812, 1303576595, 1292353896, 1280284715, 1267439395, 1253800282, 1239290227, 1224204615, 1208555750, 1192253916, 1175348075, 1158003432, 1140232197, 1122181802, 1103731002, 1084691165, 1065264715, 1045541378, 1025377500, 1004896066, 984129471, 962818701, 941450581, 920089194, 898801170, 877570573, 856548680, 835832262, 815140166, 794505981, 773691839, 753111472, 732802815, 712811714, 693047240, 673555033, 654308903, 635383943, 616766975, 598411405, 580343154, 562586049, 545046216, 527776037, 510827057, 494049507, 477578128, 461413023, 445462007, 429787126, 414416888, 399418161, 384842837, 370667635, 356946999, 343559090, 330463417, 317495364, 304708183, 292110282, 279705329, 267575791, 255709238, 244166696, 232934695, 221981601, 211312758, 200877100, 190676243, 180865949, 171343060, 162076040, 153094160, 144280792, 135660908, 127493989, 119737536, 112325687, 105343476, 98645147, 92189390, 86030260, 80107448, 74395848, 69001593, 63896756, 59034845, 54501785, 50211515, 46128131, 42348981, 38748854, 35300994, 32063547, 29031195, 26169711, 23556203, 21119357, 18827036, 16735364, 14820814, 13055391, 11540797, 10158987, 8881182, 7736529, 6688771, 5720466, 4875406, 4129139, 3461382, 2911512, 2428252, 1997875, 1647922, 1350750, 1093896, 892850, 716554, 560007, 434622, 333727, 250077, 184254, 129216, 83267, 54805, 35549, 22573, 15814, 10596, 6468, 3597, 1722, 543, 181, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		130132913776d,                          // sampleCount
		60,                                     // new dataSampleOffset
		1.2628814103961077d * DEFAULT_OFFSET,   // mean (using DEFAULT_OFFSET to be the mean of the original/superclass histogram data)
		new double[] {2.8972393955051245E-6, 5.266151666294405E-6, 7.511968777240696E-6, 9.638380832566185E-6, 1.1737817956269936E-5, 1.3740360019109943E-5, 1.5649767132419612E-5, 1.7586959323967477E-5, 1.9490091548224342E-5, 2.1340644786871366E-5, 2.324230705097463E-5, 2.500632197532937E-5, 2.673068950231894E-5, 2.8515087041380838E-5, 3.03480908780962E-5, 3.204361513686749E-5, 3.374348099027813E-5, 3.549638279176648E-5, 3.708947555880471E-5, 3.8887901726791796E-5, 4.0484733240031545E-5, 4.223352114118839E-5, 4.375256096489366E-5, 4.5515794246941965E-5, 4.697963766320257E-5, 4.867768345429013E-5, 5.027064928908587E-5, 5.187227860092847E-5, 5.342155146736971E-5, 5.506120289533456E-5, 5.657478350373779E-5, 5.813558352185666E-5, 5.961444040503139E-5, 6.124942129170918E-5, 6.285607249401082E-5, 6.427650622464753E-5, 6.578192860162702E-5, 6.7237928104813E-5, 6.883022116488391E-5, 7.014414494355137E-5, 7.163856947167702E-5, 7.323418248228464E-5, 7.465414540552828E-5, 7.622476626556463E-5, 7.745890192145666E-5, 7.910770741093032E-5, 8.04294208536227E-5, 8.192598876943791E-5, 8.343824138057343E-5, 8.469918881059968E-5, 8.631548799664657E-5, 8.752069292716769E-5, 8.903738629362223E-5, 9.053013668269827E-5, 9.17312009272031E-5, 9.309532882584293E-5, 9.45310974000968E-5, 9.605613458019135E-5, 9.731283243263888E-5, 9.86392811781478E-5, 1.0004409836250616E-4, 1.0129703525008082E-4, 1.0263355873663116E-4, 1.04006367795062E-4, 1.0535332598626823E-4, 1.0659861645789803E-4, 1.0778059587357447E-4, 1.0919284978028188E-4, 1.1034678647936502E-4, 1.1176489175079851E-4, 1.1286684777322975E-4, 1.1423414622457884E-4, 1.154365623610154E-4, 1.1674813593254285E-4, 1.1787446534539114E-4, 1.1914701749171641E-4, 1.2038148159421169E-4, 1.2150628180984668E-4, 1.2279748076915453E-4, 1.239956799898856E-4, 1.252228687262935E-4, 1.2641889794062777E-4, 1.2752379642622103E-4, 1.2877156752511E-4, 1.2978672444896588E-4, 1.310299470564935E-4, 1.3222056598083398E-4, 1.3328058371397944E-4, 1.3444298318399106E-4, 1.3561121944460908E-4, 1.3671963017225508E-4, 1.3788698263939025E-4, 1.3902648299035647E-4, 1.4004691229052568E-4, 1.411566066109593E-4, 1.4217867791499472E-4, 1.432531243952262E-4, 1.442972718908031E-4, 1.4539924403011995E-4, 1.4665157794380282E-4},	
		new double[] {0.0, 4.916967777076821E-8, 9.07144638221888E-8, 1.2903520230157285E-7, 1.66826951553022E-7, 2.0387274554023382E-7, 2.377471076622371E-7, 2.7170329080970796E-7, 3.077161417589355E-7, 3.3859352276480345E-7, 3.7614088664809896E-7, 4.094255042990611E-7, 4.394175160990875E-7, 4.7323690687224554E-7, 5.075593378752451E-7, 5.410662157267133E-7, 5.712310462741713E-7, 5.972666415943022E-7, 6.321599888979944E-7, 6.68787803844359E-7, 6.993793339705483E-7, 7.258789525648577E-7, 7.59755306335521E-7, 7.841002813304044E-7, 8.144823426151826E-7, 8.486830149079827E-7, 8.788874117873126E-7, 9.112836466738787E-7, 9.421079205224916E-7, 9.710151991283868E-7, 9.99573975169124E-7, 1.0167929948505214E-6, 1.049692749350734E-6, 1.0803240198448146E-6, 1.1123395705931417E-6, 1.1477612652157984E-6, 1.1780074289032448E-6, 1.2058867769802874E-6, 1.2296980523793166E-6, 1.257417910819827E-6, 1.2872310354908155E-6, 1.3083353042756758E-6, 1.3371980395409253E-6, 1.3692410807707495E-6, 1.3944622559677982E-6, 1.4279200866928506E-6, 1.458999685647599E-6, 1.490155074693466E-6, 1.521228981225132E-6, 1.5456303243722896E-6, 1.571251397703902E-6, 1.5998779592081413E-6, 1.6284501372923797E-6, 1.648706918372511E-6, 1.666618092605307E-6, 1.6981191091226682E-6, 1.7244051605145315E-6, 1.7571758019795683E-6, 1.790280765418314E-6, 1.8164193218808293E-6, 1.8491353280310485E-6, 1.8731236502284418E-6, 1.9047222451474691E-6, 1.929564189184364E-6, 1.9521581855725404E-6, 1.9806434150741113E-6, 2.002846948950977E-6, 2.034756935640462E-6, 2.0550934134626843E-6, 2.075840753941595E-6, 2.09908023580301E-6, 2.136307571382165E-6, 2.1630087129770137E-6, 2.1882292132987875E-6, 2.215666992874022E-6, 2.2368916327037373E-6, 2.2622861275507477E-6, 2.2890749419937576E-6, 2.323939803327135E-6, 2.3531540873396104E-6, 2.379277809678266E-6, 2.4093374615685334E-6, 2.4279668844936834E-6, 2.4601311719434063E-6, 2.4806484592644553E-6, 2.4974613702195324E-6, 2.5222709340662115E-6, 2.5451579621412114E-6, 2.5620418005696023E-6, 2.5926070300558087E-6, 2.62063841814211E-6, 2.659186241774094E-6, 2.6805270010471217E-6, 2.7032390209763934E-6, 2.729167788420624E-6, 2.7510473334001177E-6, 2.794649102616761E-6, 2.8144243347944623E-6, 2.8413060475866866E-6, 2.8693080481405623E-6}	
	);
	
	/** Pre-calculated data, see RECALCULATE_HISTOGRAM and code in the constructor if you wish to recalculate */
	HistogramData humidRiversHistogram_invarientRiverDepth = new HistogramData(
		new long[] {0, 43, 282, 758, 2427, 5100, 8922, 14629, 21963, 31147, 49525, 77312, 115892, 180637, 258052, 349256, 468324, 614831, 792416, 1019726, 1279131, 1574097, 1959412, 2412505, 2941564, 3607399, 4370948, 5246928, 6340817, 7596577, 9026999, 10711224, 12596144, 14695307, 17105410, 19809399, 22839639, 26424606, 30421812, 34875377, 39882853, 45347625, 51297533, 57910770, 65126036, 73011541, 81591457, 90730755, 100468282, 111032494, 122261070, 134108506, 146812965, 160316058, 174655213, 189946655, 206026460, 222777025, 240283168, 258435891, 277358294, 297531761, 318952642, 341414622, 364561998, 387963226, 411765701, 436226280, 461133048, 486431131, 512360105, 538621093, 565147304, 591865159, 618612321, 645281130, 672077687, 699029216, 726163054, 753283923, 780163445, 806731722, 832849891, 858643826, 884038664, 909198329, 933985048, 958426383, 982484781, 1005982630, 1028817518, 1051011662, 1072699229, 1093596923, 1113297021, 1132304789, 1150836500, 1168841417, 1186299735, 1202950276, 1218657125, 1233600625, 1247923594, 1261440282, 1274235245, 1286305323, 1297657709, 1308430897, 1318726600, 1327896105, 1336081878, 1343351124, 1349935412, 1355962095, 1361593512, 1366790124, 1371282217, 1375066634, 1377696774, 1379482953, 1380518991, 1380613105, 1379936255, 1378386995, 1375881313, 1372811756, 1369308586, 1365184581, 1360205700, 1354294888, 1347693258, 1340618854, 1332958827, 1324455242, 1315204020, 1305249656, 1294437776, 1282857696, 1270502146, 1257279136, 1243547813, 1229319518, 1214480193, 1199044978, 1183173469, 1166848079, 1150257961, 1133292880, 1115719783, 1097785862, 1079572815, 1060864439, 1041788767, 1022382592, 1002306837, 982147955, 961973971, 941876139, 921858626, 902057550, 882542674, 862946407, 843311365, 823336845, 803552575, 783997581, 764706618, 745559199, 726607091, 707812454, 689283320, 671009346, 652915009, 635029652, 617380916, 599851026, 582496759, 565372300, 548291992, 531441308, 514823945, 498322336, 482013637, 465935060, 450180243, 434821810, 419835572, 405299099, 391064520, 377085050, 363149237, 349330093, 335641136, 322088325, 308780798, 295706605, 282948750, 270493263, 258302807, 246386876, 234681694, 223191331, 212120057, 201337227, 190804867, 180562829, 170461856, 160535035, 151120679, 142167095, 133593877, 125514216, 117742436, 110228064, 103042988, 96111049, 89402016, 83055176, 77036483, 71288963, 65925986, 60838363, 55982210, 51485475, 47188061, 43057092, 39169063, 35518424, 32063040, 28904277, 25951444, 23164823, 20618281, 18282699, 16123769, 14274678, 12584328, 11016912, 9611020, 8320568, 7123998, 6078735, 5153935, 4324428, 3641935, 3040860, 2504150, 2067965, 1697052, 1375861, 1124807, 904053, 707382, 549754, 422735, 317220, 234093, 164351, 105911, 69746, 45247, 28731, 20165, 13536, 8277, 4612, 2211, 698, 233, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},			
		132947088383d,                          // sampleCount
		60,                                     // new dataSampleOffset
		1.2901914748723757d * DEFAULT_OFFSET,   // mean (using DEFAULT_OFFSET to be the mean of the original/superclass histogram data)
		new double[] {2.8937902665227E-6, 5.263153338486148E-6, 7.499426793788899E-6, 9.58361869374258E-6, 1.166676470321093E-5, 1.3620730167307471E-5, 1.5518491168098336E-5, 1.747112428995834E-5, 1.9356349131083306E-5, 2.1140557967758156E-5, 2.298696126645794E-5, 2.4773169740921475E-5, 2.6530851478856476E-5, 2.8165682880665344E-5, 3.0049039462465288E-5, 3.174049871601525E-5, 3.3344188025363763E-5, 3.511942583811489E-5, 3.66831320550709E-5, 3.834856594364666E-5, 3.993355191544978E-5, 4.161305857186548E-5, 4.3100032132148414E-5, 4.483020965695417E-5, 4.6365915798762813E-5, 4.791973807938277E-5, 4.948397439141772E-5, 5.1007872743813465E-5, 5.247553220000256E-5, 5.403296401285118E-5, 5.554875587376884E-5, 5.7169380209831644E-5, 5.839936459883788E-5, 6.002467098253916E-5, 6.152863448294354E-5, 6.309707166840539E-5, 6.448999240449282E-5, 6.582969253221549E-5, 6.738467372668921E-5, 6.875942282702496E-5, 7.028646026396856E-5, 7.172911098818346E-5, 7.302717198287567E-5, 7.454555947683517E-5, 7.573165509322521E-5, 7.726366452720512E-5, 7.856197426781009E-5, 8.014492409502694E-5, 8.145519260027712E-5, 8.284391520563261E-5, 8.422823734610674E-5, 8.55477498746815E-5, 8.698798155437615E-5, 8.83465695546981E-5, 8.963989668364647E-5, 9.108235084598348E-5, 9.228464237454048E-5, 9.363658070293823E-5, 9.50295893242803E-5, 9.628421502361206E-5, 9.763588345945706E-5, 9.887106763750708E-5, 1.0030177449271704E-4, 1.0162149530978937E-4, 1.0278207969305342E-4, 1.0399685865580821E-4, 1.0523670728934675E-4, 1.066083728686884E-4, 1.0769273171939986E-4, 1.0904691824586976E-4, 1.1018094816561685E-4, 1.115351818561372E-4, 1.1277020014643924E-4, 1.1386092937692993E-4, 1.1516449968818065E-4, 1.1629714151029091E-4, 1.1750224983897439E-4, 1.1867782997621035E-4, 1.1988674442564573E-4, 1.2098141326122303E-4, 1.2219098029797593E-4, 1.2337180328225728E-4, 1.245299801770905E-4, 1.2569248803506738E-4, 1.2673578507049754E-4, 1.2797140477216473E-4, 1.290153060939936E-4, 1.3013652165883016E-4, 1.3131298459745366E-4, 1.324641005999716E-4, 1.3355092799782753E-4, 1.3470427952855246E-4, 1.3574548585415513E-4, 1.367809460828262E-4, 1.3777150674831873E-4, 1.3883596031538659E-4, 1.3993819030089227E-4, 1.4101180104342248E-4, 1.4205956035029388E-4, 1.4326985770167673E-4},
		new double[] {0.0, 4.970423241716411E-8, 9.219523296933629E-8, 1.3039723784385412E-7, 1.658292756790918E-7, 2.0370349037834888E-7, 2.3700584704551616E-7, 2.737951924216519E-7, 3.0798769309704985E-7, 3.424496091229038E-7, 3.75697571629678E-7, 4.1184379097404495E-7, 4.39899884327682E-7, 4.74288144314248E-7, 5.09821967046105E-7, 5.392729936375257E-7, 5.705314100001782E-7, 6.005666287662307E-7, 6.360766270523385E-7, 6.648537932360841E-7, 7.015697078178822E-7, 7.294360377954384E-7, 7.573062025476017E-7, 7.86525815393188E-7, 8.161469608688591E-7, 8.534527502042218E-7, 8.823810683365607E-7, 9.138432466795576E-7, 9.435839733393313E-7, 9.712737094770572E-7, 9.924710761055627E-7, 1.0234703905694623E-6, 1.0550394258600584E-6, 1.0899052955906557E-6, 1.1174087180714042E-6, 1.1420621517945174E-6, 1.1781176033684396E-6, 1.210562352497336E-6, 1.2347852275610305E-6, 1.2618621130451022E-6, 1.2794438621191856E-6, 1.3064971111616367E-6, 1.3349172475617981E-6, 1.371214295962426E-6, 1.4052757643772902E-6, 1.4341642238414447E-6, 1.4635791664410652E-6, 1.4858633800037826E-6, 1.5151521655225172E-6, 1.5393617523196001E-6, 1.5703942027996581E-6, 1.5939545048248272E-6, 1.6197050194768254E-6, 1.6458432229902682E-6, 1.6723256750205136E-6, 1.696406232238548E-6, 1.7299138163419755E-6, 1.7622164248795934E-6, 1.7854714353023187E-6, 1.8185527536207344E-6, 1.8489760607972346E-6, 1.869566313177572E-6, 1.8959582865861043E-6, 1.9209865994204787E-6, 1.9520911726241206E-6, 1.9857554616000866E-6, 2.0043821588837155E-6, 2.023560929698875E-6, 2.040840861331379E-6, 2.066098205376908E-6, 2.1077240616016175E-6, 2.125870235985375E-6, 2.1583708774388512E-6, 2.1823550929189344E-6, 2.2159779395469416E-6, 2.245747141984053E-6, 2.2758982008956598E-6, 2.3044188809704056E-6, 2.3237564565446453E-6, 2.3495243134949786E-6, 2.369372128667024E-6, 2.3920891654168752E-6, 2.4209153562373867E-6, 2.447381639507428E-6, 2.470432390931504E-6, 2.488709294824264E-6, 2.5109596385677654E-6, 2.541931702731018E-6, 2.570116054533462E-6, 2.6062745556027576E-6, 2.628315166297358E-6, 2.654856701797825E-6, 2.6807308604092556E-6, 2.701281347583851E-6, 2.7225228099661718E-6, 2.750489881290982E-6, 2.77590863543319E-6, 2.807176898794501E-6, 2.8380945779383724E-6, 2.8728450680993577E-6}
	);

	
	/**
	 * Calculates the compound distribution of processedHistogram_Humidity combined with a sampledHumidityScale
	 * 
	 * The compound distribution data is already written to valleysHumidityHistogram_invarientRiverDepth and
	 * valleysHumidityHistogram_varientRiverDepth. This 
	 */
	private void calculateAdjustedSamples(boolean vary_river_depth) {
		
		float maxScaling = sampledHumidityScale_rangeMax; // The valleys mapgen rivers can only raise humidity by 1.5 times, with its current formula.
		
		int    original_dataSampleOffset   = processedDataSampleOffset_Humidity;
		long[] original_Histogram_Humidity = processedHistogram_Humidity;		
				
		int newBucketCount                 = (int) Math.ceil(original_Histogram_Humidity.length * maxScaling + 1); // +1 to be safe since the new dataSampleOffset has been rounded up
		int newDataSampleOffset_Humidity   = (int) Math.ceil(original_dataSampleOffset * maxScaling);	
		double[] workingHistogram_Humidity = new double[newBucketCount];
				
		long[] humidityScale = vary_river_depth ? sampledHumidityScale_varientRiverDepth : sampledHumidityScale_invarientRiverDepth;
		double increment = (double)(sampledHumidityScale_rangeMax - sampledHumidityScale_rangeMin) / humidityScale.length;
		
		long humidityScale_samplesCount = 0;
		for(int i = 0; i < humidityScale.length; i++) humidityScale_samplesCount += humidityScale[i];
		
		for(int destIndex = 0; destIndex < workingHistogram_Humidity.length; destIndex++) { // Fill each of the new sample-buckets
		
			long destValue = destIndex - newDataSampleOffset_Humidity;
			
			for(int i = 0; i < humidityScale.length; i++) {
				double scaleValue = sampledHumidityScale_rangeMin + i * increment;
				//double sourceValue = (destValue / scaleValue) / 0.8d; // Not scaling it by 0.8 here as I think it should be the same if you scale the final histogram, but with less accuracy lost. 
				double sourceValue = destValue / scaleValue;
				
				//linearly interpolate the sourceCount from the closest two source buckets
				int lowerBucket = (int) Math.floor(sourceValue);				
				int upperBucket = (int) Math.ceil(sourceValue);
				if (lowerBucket == upperBucket) upperBucket = lowerBucket + 1;
				double lowerBucketPortion = 1 - (sourceValue - lowerBucket);
				double upperBucketPortion = 1 - (upperBucket - sourceValue);
				
				int bucketIndex = lowerBucket + original_dataSampleOffset;
				if (bucketIndex >= 0 && bucketIndex < original_Histogram_Humidity.length) {
					workingHistogram_Humidity[destIndex] += original_Histogram_Humidity[bucketIndex] * lowerBucketPortion * humidityScale[i] / (double)humidityScale_samplesCount;
				}
				bucketIndex = upperBucket + original_dataSampleOffset;
				if (bucketIndex >= 0 && bucketIndex < original_Histogram_Humidity.length) {
					workingHistogram_Humidity[destIndex] += original_Histogram_Humidity[bucketIndex] * upperBucketPortion * humidityScale[i] / (double)humidityScale_samplesCount;
				}				
			}
		}
		
		HistogramData results = vary_river_depth ? humidRiversHistogram_varientRiverDepth : humidRiversHistogram_invarientRiverDepth;
		
		results.samples = new long[workingHistogram_Humidity.length];	
		long sampleCount = 0;
		double floatSampleCount = 0;
		for(int i = 0; i < workingHistogram_Humidity.length; i++) {
			long samplesInBucket = Math.round(workingHistogram_Humidity[i]);
			results.samples[i] = samplesInBucket;
			sampleCount += samplesInBucket;
			floatSampleCount += workingHistogram_Humidity[i];
		}
		results.samplesCount = sampleCount; // this differs depending on vary_river_depth - cumulative rounding errors???
		results.sampleOffset = newDataSampleOffset_Humidity;
		
		// find mean humidity
		double sum = 0;
		for(int i = 0; i < humidityScale.length; i++) {
			double representedValue = sampledHumidityScale_rangeMin + i * increment;
			sum += representedValue * humidityScale[i];
		}
		double scalingMean = sum / humidityScale_samplesCount;
		results.mean = scalingMean * DEFAULT_OFFSET; // Using DEFAULT_OFFSET to be the mean of original_Histogram_Humidity
		
		// find median humidity
		sum = 0;
		for(int i = 0; i < results.samples.length; i++) {
			sum += results.samples[i];
			if ((sum * 2) > results.samplesCount) {
				results.median = (i - 0.5) - newDataSampleOffset_Humidity;
				break;
			}
		}				
		
		AmidstLogger.info("Results for when vary_river_depth = " + vary_river_depth + ":");		
		AmidstLogger.info("    samples[]: " + Arrays.toString(results.samples));
		AmidstLogger.info("    samplesCount: " + results.samplesCount + " (" + floatSampleCount + ")");
		AmidstLogger.info("    sampleOffset: " + results.sampleOffset);
		AmidstLogger.info("    mean: " + scalingMean + " * DEFAULT_OFFSET; // Using DEFAULT_OFFSET to be the mean of original_Histogram_Humidity");
		AmidstLogger.info("    median: " + results.median);
	}
	
	
	/**
	 * Constructor
	 * @param humid_rivers     FLAG_VALLEYS_HUMID_RIVERS in the mapgen params
	 * @param vary_river_depth FLAG_VALLEYS_VARY_RIVER_DEPTH in the mapgen params
	 */
	public ClimateHistogram_ValleysHumidRivers(boolean humid_rivers, boolean vary_river_depth) {

		if (RECALCULATE_HISTOGRAM) {
			// the valleysHumidityHistograms are already processed, and the percentile tables 
			// already calculated, but I leave this code here in case someone wants to update
			// valleysHumidityHistogram_invarientRiverDepth or valleysHumidityHistogram_varientRiverDepth 
			// etc with their own sampled data and recalculate.
			calculateAdjustedSamples(false);
			calculateAdjustedSamples(true);
		}
		
		if (humid_rivers) {						
			// change the data samples used by the superclass so that its distribution 
			// includes the effects of humid_rivers.
			super.getSampleMean(); // so we can write new value straight into sampleMean.y
			HistogramData newHumidityHistogram = vary_river_depth ? humidRiversHistogram_varientRiverDepth : humidRiversHistogram_invarientRiverDepth;
			
			processedHistogram_Humidity        = newHumidityHistogram.samples;
			processedDataSampleCount_Humidity  = newHumidityHistogram.samplesCount;
			processedDataSampleOffset_Humidity = newHumidityHistogram.sampleOffset;
			sampleMean.y                       = newHumidityHistogram.mean;
			frequencyAtPercentileTable         = newHumidityHistogram.frequencyAtPercentileTable;
			frequencyAtPerdimileTable          = newHumidityHistogram.frequencyAtPerdimileTable;
		} else {
			// do nothing - the ClimateHistogram doesn't change if humid_rivers is off 
		}
		
		if (RECALCULATE_HISTOGRAM) {
			// We wait until the histogram data in the superclass has been updated before 
			// letting it calculate the percentile tables.
			calculatePercentileTables(50);		
		}
	}
	
}
