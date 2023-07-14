// @ts-nocheck
/* eslint-disable */

export const WASTE_WATER_SURVEILLANCE = {
	DESCRIPTION: 'COVID-19 Wastewater Surveillance: An Epidemiological Model',
	AUTHOR_NAME: 'Shakeri-Lab',
	AUTHOR_EMAIL: 'UNKNOWN',
	DATE: '2021-10-19',
	SCHEMA: 'dates,VAX_count,day,sdm,events,I_1,I_2,I_3,Y_1,Y_2,Y_3,V_1,V_2,V_3,Infected,Y,V,logV',
	PROVENANCE:
		'Using wastewater surveillance as a continuous pooled sampling technique has been in place in many countries since the early stages of the outbreak of COVID-19.',
	SENSITIVITY: 'UNKNOWN',
	EXAMPLES:
		'{"dates": "2021-05-14", "VAX_count": 236, "day": 430, "sdm": 0.4, "events": 0.1, "I_1": 85, "I_2": 49, "I_3": 62, "Y_1": 3, "Y_2": 0, "Y_3": 2, "V_1": 113192.37284202829, "V_2": 81612.73344185714, "V_3": 78832.64723781461, "Infected": 196, "Y": 5, "V": 87568.41792704807, "logV": 11.38017568597244}',
	LICENSE: 'UNKNOWN',
	DATA_PROFILING_RESULT: {
		dates: {
			col_name: 'dates',
			concept: 'Time',
			unit: 'YYYY-MM-DD',
			description: 'The date when the data was recorded',
			dkg_groundings: [
				['hp:0001147', 'Retinal exudate'],
				['hp:0030496', 'Macular exudate'],
				['ncit:C114947', 'Postterm Infant'],
				['oae:0006126', 'retinal exudates AE'],
				['oae:0008293', 'large for dates baby AE'],
				['pato:0000165', 'time', 'class'],
				['gfo:Time', 'time', 'class'],
				['geonames:2365173', 'Maritime', 'individual'],
				['wikidata:Q174728', 'centimeter', 'class'],
				['probonto:k0000056', 'nondecisionTime', 'class']
			]
		},
		VAX_count: {
			col_name: 'VAX_count',
			concept: 'Vaccination',
			unit: 'Count',
			description: 'The number of vaccinations administered on the given date',
			dkg_groundings: [
				['apollosv:00000142', 'vaccination', 'class'],
				['idomal:0001040', 'vaccination', 'class'],
				['vo:0000002', 'vaccination', 'class'],
				['askemo:0000012', 'vaccination rate', 'class'],
				['vo:0000814', 'vaccination dose', 'class']
			]
		},
		day: {
			col_name: 'day',
			concept: 'Time',
			unit: 'Day',
			description: 'The day count since the start of the data recording',
			dkg_groundings: [
				['wikidata:Q573', 'day'],
				['geonames:3099434', 'Gdańsk'],
				['geonames:1263814', 'Mandya'],
				['geonames:1275163', 'Budaun'],
				['geonames:358269', 'Dayrūţ'],
				['pato:0000165', 'time', 'class'],
				['gfo:Time', 'time', 'class'],
				['geonames:2365173', 'Maritime', 'individual'],
				['wikidata:Q174728', 'centimeter', 'class'],
				['probonto:k0000056', 'nondecisionTime', 'class']
			]
		},
		sdm: {
			col_name: 'sdm',
			concept: 'Social Distancing Measure',
			unit: 'Ratio',
			description: 'The level of social distancing measures in place, represented as a ratio',
			dkg_groundings: [
				['geonames:2852458', 'Potsdam'],
				['pr:Q9D8T2', 'gasdermin-D (mouse)'],
				['pr:P57764', 'gasdermin-D (human)'],
				['pr:Q14696', 'LRP chaperone MESD (human)'],
				['cido:0013402', 'SARS-CoV-2-human S-DMD physical association'],
				[
					'wikidata:Q91104866',
					'social distancing measures related to the 2019–20 coronavirus pandemic',
					'class'
				],
				[
					'wikidata:Q96612699',
					'Social distancing measures: evidence of interruption of seasonal influenza activity and early lessons of the SARS-CoV-2 pandemic',
					'class'
				],
				[
					'wikidata:Q99612241',
					'Social distancing measures in the fight against COVID-19 in Brazil: description and epidemiological analysis by state',
					'class'
				],
				[
					'wikidata:Q104138004',
					'Social distancing measures and demands for the reorganization of hemotherapy services in the context of Covid-19',
					'class'
				],
				[
					'wikidata:Q96290984',
					'Social distancing measures to control the COVID-19 pandemic: potential impacts and challenges in Brazil',
					'class'
				]
			]
		},
		events: {
			col_name: 'events',
			concept: 'Events',
			unit: 'Count',
			description:
				'The number of significant events (such as policy changes or outbreaks) on the given date',
			dkg_groundings: [
				['hp:0001907', 'Thromboembolism'],
				['hp:0001297', 'Stroke'],
				['pr:Q9GZR2', 'RNA exonuclease 4 (human)'],
				['cemo:number_of_cases_with_exposure_events', 'number of cases with exposure events'],
				['cemo:number_of_schools_with_exposure_events', 'number of schools with exposure events']
			]
		},
		I_1: {
			col_name: 'I_1',
			concept: 'Infectious Population',
			unit: 'Count',
			description:
				'The number of infectious individuals in the first compartment of the SEIR model',
			dkg_groundings: [
				['chebi:189658', '4EGI-1'],
				['vo:0011274', 'LmSTI1'],
				['ogg:3001197075', 'BMEI1364'],
				['probonto:k0001243', 'Chi1'],
				['probonto:k0000361', 'ParetoTypeI1']
			]
		},
		I_2: {
			col_name: 'I_2',
			concept: 'Infectious Population',
			unit: 'Count',
			description:
				'The number of infectious individuals in the second compartment of the SEIR model',
			dkg_groundings: [
				['vo:0005238', 'VBI-2902a'],
				['probonto:k0000028', 'Bernoulli2'],
				['chebi:189653', 'pelabresib'],
				['pr:P08582', 'melanotransferrin (human)'],
				['pr:Q15477', 'helicase SKI2W (human)']
			]
		},
		I_3: {
			col_name: 'I_3',
			concept: 'Infectious Population',
			unit: 'Count',
			description:
				'The number of infectious individuals in the third compartment of the SEIR model',
			dkg_groundings: [
				['chebi:82721', 'dalbavancin'],
				['pr:Q13501', 'sequestosome-1 (human)'],
				['doid:0111936', 'immunodeficiency 14'],
				['pr:Q8WY22', 'BRI3-binding protein (human)'],
				['cido:0013240', 'SARS-CoV-2-human S-ERI3 association']
			]
		},
		Y_1: {
			col_name: 'Y_1',
			concept: 'Case Count',
			unit: 'Count',
			description: 'The number of confirmed cases in the first compartment of the SEIR model',
			dkg_groundings: [
				['probonto:k0001393', 'Levy1'],
				['doid:0060728', 'NGLY1-deficiency'],
				['probonto:k0000274', 'Cauchy1'],
				['chebi:145798', 'ouabain(1-)'],
				['chebi:145795', 'digoxin(1-)'],
				[
					'wikidata:Q36803786',
					'Case counting in epidemiology: limitations of methods based on multiple data sources',
					'class'
				]
			]
		},
		Y_2: {
			col_name: 'Y_2',
			concept: 'Case Count',
			unit: 'Count',
			description: 'The number of confirmed cases in the second compartment of the SEIR model',
			dkg_groundings: [
				['vo:0012363', 'AY243312'],
				['idomal:0001137', 'Py235'],
				['chebi:93829', '4-[1-hydroxy-2-[4-(phenylmethyl)-1-piperidinyl]propyl]phenol'],
				['chebi:32458', 'cysteinium'],
				['chebi:32613', 'isoleucinium'],
				[
					'wikidata:Q36803786',
					'Case counting in epidemiology: limitations of methods based on multiple data sources',
					'class'
				]
			]
		},
		Y_3: {
			col_name: 'Y_3',
			concept: 'Case Count',
			unit: 'Count',
			description: 'The number of confirmed cases in the third compartment of the SEIR model',
			dkg_groundings: [
				['chebi:82699', 'oritavancin'],
				['chebi:95341', 'baricitinib'],
				['chebi:180654', 'N(4)-hydroxycytidine'],
				['chebi:28001', 'vancomycin'],
				['chebi:2955', 'azithromycin'],
				[
					'wikidata:Q36803786',
					'Case counting in epidemiology: limitations of methods based on multiple data sources',
					'class'
				]
			]
		},
		V_1: {
			col_name: 'V_1',
			concept: 'Viral Load',
			unit: 'Count',
			description: 'The viral load in the first compartment of the SEIR model',
			dkg_groundings: [
				['ncit:C171133', 'COVID-19 Infection'],
				['idomal:0000883', 'SV1'],
				['vo:0005227', 'BBV154'],
				['vo:0004774', 'NYVAC-HIV-1'],
				['vo:0004996', 'AdCLD-CoV19'],
				['vido:0001331', 'viral load', 'class'],
				['vsmo:0000051', 'viral load of arthropod', 'class']
			]
		},
		V_2: {
			col_name: 'V_2',
			concept: 'Viral Load',
			unit: 'Count',
			description: 'The viral load in the second compartment of the SEIR model',
			dkg_groundings: [
				['ncit:C171133', 'COVID-19 Infection'],
				['vo:0005255', 'ABNCoV2'],
				['vo:0005169', 'GRAd-COV2'],
				['vo:0005196', 'VXA-CoV2-1'],
				['vo:0005263', 'CoV2-OGEN1'],
				['vido:0001331', 'viral load', 'class'],
				['vsmo:0000051', 'viral load of arthropod', 'class']
			]
		},
		V_3: {
			col_name: 'V_3',
			concept: 'Viral Load',
			unit: 'Count',
			description: 'The viral load in the third compartment of the SEIR model',
			dkg_groundings: [
				['vo:0004656', 'rBCGpMV361-TgCyP'],
				['vo:0004695', 'PAV3-HA (H5N1)'],
				['ncbitaxon:1403205', 'Coronavirus PgCoV-3'],
				['ncbitaxon:1289548', 'Coronavirus Mex_CoV-3'],
				['ncbitaxon:2717647', 'Coronavirus PREDICT_CoV-35'],
				['vido:0001331', 'viral load', 'class'],
				['vsmo:0000051', 'viral load of arthropod', 'class']
			]
		},
		Infected: {
			col_name: 'Infected',
			concept: 'Infection',
			unit: 'Count',
			description: 'The total number of infected individuals on the given date',
			dkg_groundings: [
				['ido:0000511', 'infected population'],
				['efo:0001460', 'uninfected'],
				['vsmo:0000268', 'infected'],
				['doid:13117', 'paronychia'],
				['idomal:0001129', 'RESA-155'],
				['ncit:C171133', 'COVID-19 Infection', 'class'],
				['ncit:C128320', 'Infection', 'class'],
				['apollosv:00000114', 'infection', 'class'],
				['ido:0000586', 'infection', 'class'],
				['ncit:C177387', 'Reinfection', 'class']
			]
		},
		Y: {
			col_name: 'Y',
			concept: 'Case Count',
			unit: 'Count',
			description: 'The total number of confirmed cases on the given date',
			dkg_groundings: [
				['ncit:C171133', 'COVID-19 Infection'],
				['ido:0000621', 'acquired immunity to infectious agent'],
				['ido:0000569', 'asymptomatic host of infectious agent'],
				['geonames:1848522', 'Yao'],
				['geonames:363619', 'Yei'],
				[
					'wikidata:Q36803786',
					'Case counting in epidemiology: limitations of methods based on multiple data sources',
					'class'
				]
			]
		},
		V: {
			col_name: 'V',
			concept: 'Viral Load',
			unit: 'Count',
			description: 'The total viral load on the given date',
			dkg_groundings: [
				['ncit:C171133', 'COVID-19 Infection'],
				['ncit:C53511', 'Intensive Care Unit'],
				['ido:0000597', 'host exposure to environment containing infectious agent'],
				['ncit:C154475', 'Discharged Alive'],
				['geonames:298117', 'Van'],
				['vido:0001331', 'viral load', 'class'],
				['vsmo:0000051', 'viral load of arthropod', 'class']
			]
		},
		logV: {
			col_name: 'logV',
			concept: 'Viral Load',
			unit: 'Logarithmic Scale',
			description: 'The total viral load on the given date, represented on a logarithmic scale',
			dkg_groundings: [
				['wikidata:Q2589313', 'Logvino'],
				['wikidata:Q4265044', 'Logvin'],
				['wikidata:Q21337279', 'Logvinovka'],
				['wikidata:Q4265064', 'Logvinivka'],
				['wikidata:Q12083595', 'Logvyn F. Boiko'],
				['vido:0001331', 'viral load', 'class'],
				['vsmo:0000051', 'viral load of arthropod', 'class']
			]
		}
	}
};
