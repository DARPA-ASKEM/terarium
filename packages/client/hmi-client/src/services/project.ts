/**
 * Project
 */

import API from '@/api/api';
import { IProject, ProjectAssets } from '@/types/Project';
import { logger } from '@/utils/logger';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { Component } from 'vue';
import * as EventService from '@/services/event';
import { EventType, Project, AssetType, ExternalPublication } from '@/types/Types';

/**
 * Create a project
 * @param name Project['name']
 * @param [description] Project['description']
 * @param [username] Project['username']
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function create(
	name: Project['name'],
	description: Project['description'] = '',
	username: Project['username'] = ''
): Promise<Project | null> {
	try {
		const project: Project = {
			name,
			description,
			username,
			active: true,
			assets: {} as Project['assets']
		};
		const response = await API.post(`/projects`, project);
		const { status, data } = response;
		if (status !== 201) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

async function update(project: IProject): Promise<IProject | null> {
	try {
		const { id, name, description, active, username } = project;
		const response = await API.put(`/projects/${id}`, { id, name, description, active, username });
		const { status, data } = response;
		if (status !== 200) {
			return null;
		}
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Remove a project (soft-delete)
 * @param projectId {IProject["id"]} - the id of the project to be removed
 * @return boolean - if the removal was succesful
 */
async function remove(projectId: IProject['id']): Promise<boolean> {
	try {
		const { status } = await API.delete(`/projects/${projectId}`);
		return status === 200;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

/**
 * Get all projects
 * @return Array<Project>|null - the list of all projects, or null if none returned by API
 */
async function getAll(): Promise<Project[] | null> {
	try {
		const response = await API.get(`/projects`);
		const { status, data } = response;
		if (status !== 200 || !data) return null;
		return (data as Project[]).reverse();
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get project assets for a given project per id
 * @param projectId projet id to get assets for
 * @param types optional list of types. If none are given we assume you want it all
 * @return ProjectAssets|null - the appropriate project, or null if none returned by API
 */
async function getAssets(projectId: string, types?: string[]): Promise<ProjectAssets | null> {
	try {
		let url = `/projects/${projectId}/assets`;
		if (types) {
			types.forEach((type, indx) => {
				// add URL with format: ...?types=A&types=B&types=C
				url += `${indx === 0 ? '?' : '&'}types=${type}`;
			});
		} else {
			Object.values(AssetType).forEach((type, indx) => {
				if (type !== AssetType.Documents) url += `${indx === 0 ? '?' : '&'}types=${type}`;
			});
		}
		const response = await API.get(url);
		const { status, data } = response;
		if (status !== 200) return null;
		data.documents = [
			{
				id: '10e4d285-10dc-4a69-9c23-12e48f1a26b8',
				name: 'paper.pdf',
				username: 'extraction_service',
				description: 'paper.pdf',
				timestamp: '2023-10-03T16:40:45',
				file_names: ['paper.pdf'],
				metadata: {},
				document_url: null,
				source: null,
				text: '\nN E T W O R K   S C I E N C E\nLack of practical identifiability may hamper reliable predictions in COVID-19 epidemic models\npredictions in COVID-19 epidemic models Luca Gallo1,2, Mattia Frasca3,4*, Vito Latora1,2,5,6, Giovanni Russo7\nCompartmental models are widely adopted to describe and predict the spreading of infectious diseases. The unknown parameters of these models need to be estimated from the data. Furthermore, when some of the model variables are not empirically accessible, as in the case of asymptomatic carriers of coronavirus disease 2019 (COVID-19), they have to be obtained as an outcome of the model. Here, we introduce a framework to quantify how the uncertainty in the data affects the determination of the parameters and the evolution of the unmeasured variables of a given model. We illustrate how the method is able to characterize different regimes of identifiability, even in models with few compartments. Last, we discuss how the lack of identifiability in a realistic model for COVID-19 may prevent reliable predictions of the epidemic dynamics.\nINTRODUCTION The  pandemic  caused  by  severe  acute  respiratory  syndrome coronavirus-2 is challenging humanity in an unprecedented way (1), with the disease, which in a few months has spread around the world, affecting large parts of the population (2, 3) and often requir- ing hospitalization or even intensive care (4, 5). Mitigating the impact of coronavirus disease 2019 (COVID-19) urges synergistic efforts to understand, predict, and control the many, often elusive, facets of the complex phenomenon of the spreading of a previously unknown virus, from RNA sequencing to the study of the virus pathogenicity and  transmissibility  (6,  7)  to  the  definition  of  suitable  epidemic spreading models (8) and the investigation of nonpharmaceutical intervention policies and containment measures (9–12). In particu- lar, a large number of epidemic models have recently been proposed to describe the evolution of COVID-19 and evaluate the effectiveness of different counteracting measures, including social distancing, testing, and contact tracing (13–19). However, even the adoption of well-consolidated modeling techniques, such as the use of mecha- nistic models at the population level based on compartments, poses fundamental problems. First of all, the very same choice of the dynamical variables to use in a compartmental model is crucial; as such, variables should adequately capture the spreading mecha- nisms and need to be tailored to the specific disease. This step is not straightforward, especially when the spreading mechanisms of the disease are still unknown or only partially identified. In addition, some of the variables considered might be difficult to measure and track as, for instance, in the case of COVID-19, it occurred in the number of individuals showing mild or no symptoms. Second, compartmental models, usually, involve a number of parameters, including the initial values of the unmeasured variables, which are not known and need to be estimated from data.\n1Department of Physics and Astronomy, University of Catania, Catania 95125, Italy. 2INFN Sezione di Catania, Via S. Sofia, 64, Catania 95125, Italy. 3Department of Elec- trical, Electronics and Computer Science Engineering, University of Catania, Catania 95125, Italy. 4Istituto di Analisi dei Sistemi ed Informatica "A. Ruberti," Consiglio Nazionale delle Ricerche (IASI-CNR), 00185 Roma 00185, Italy. 5School of Mathe- matical Sciences, Queen Mary University of London, London E1 4NS, UK 6Complexity Science Hub Vienna, A-1080 Vienna, Austria. 7Department of Mathematics and Computer Science, University of Catania, Catania 95125, Italy. *Corresponding author. Email: mattia.frasca@dieei.unict.it\n\nHaving at disposal a large amount of data, unfortunately, does not simplify the problem of parameter estimation and prediction of unmeasured states. Once a model is formulated, it may occur that some of its unknown parameters are intrinsically impossible to determine from the measured variables or that they are numerically very sensitive to the measurements themselves. In the first case, it is the very same structure of the model to hamper parameter estima- tion as the system admits infinitely many sets of parameters that fit the data equally well; for this reason, this problem is referred to as structural identifiability (20, 21). In the second case, although under ideal  conditions  (i.e.,  noise-free  data  and  error-free  models)  the problem of parameter estimation can be uniquely solved for some trajectories, it may be numerically ill conditioned, such that from a practical point of view, the parameters cannot be determined with precision even if the model is structurally identifiable (22). This situation typically occurs when large changes in the parameters entail a small variation of the measured variables, such that two similar trajectories may correspond to very different parameters (23). The term practical identifiability is adopted in this case. Identifiability in general represents an important property of a dynamical system, as in a nonidentifiable system, different sets of parameters can produce the same or very similar fits of the data. Consequently, predictions from a nonidentifiable system become unreliable. In the context of epidemics forecasting, this means that even  if  the  model  considered  is  able  to  reproduce  the  measured variables, a large uncertainty may affect the estimated values of the parameters and the predicted evolution of the unmeasured variables (24). The problem of practical identifiability of model parameters has been investigated using different methodologies based on Fisher\'s information theory (25, 26), profile likelihood (27), Monte Carlo simulations (28), and other computational approaches (29). How- ever, the lack of practical identifiability can also affect the reliability of the prediction of the unmeasured variables dynamics (27), an issue of utmost importance in the context of COVID-19, which nevertheless still requires a systematic investigation. In particular, an approach to simultaneously characterize the problem of sensitivity to parameters and that of the reliability of predictions of unmeasured variables is still missing. In more detail, in this paper, we investigate the problem of the practical identifiability of dynamical systems whose state includes\nCopyright © 2022 The Authors, some rights reserved; exclusive licensee American Association for the Advancement of Science. No claim to original U.S. Government Works. Distributed under a Creative Commons Attribution License 4.0 (CC BY).\n1 of 13\n\nnot only measurable but also hidden variables, as is the case of com- partment models for COVID-19 epidemic. We present a general framework to quantify not only the sensitivity of the measured variables of a given model on its parameters but also the sensitiv- ity of the unmeasured variables on the parameters and on the measured variables. This will allow us to introduce the notion of practical identifiability of the hidden variables of a model. As a relevant and timely application, we show the variety of different regimes and levels of identifiability that can appear in epidemic models, even in the simplest case of a four compartment system. Last, we study the actual effects of the lack of practical identifiability in more sophisticated models introduced for COVID-19.\nRESULTS Dynamical systems with hidden variables Consider the n-dimensional dynamical system described by the following equations\nm ̇ = f(m, h, q ) ,        (1) h ̇ = g(m, h, q) m ∈ ℝnm that can be empirically accessed (measurable variables)\nwhere we have partitioned the state variables into two sets: the variables m ∈ ℝnm that can be empirically accessed (measurable variables) The dynamics of the system is governed by the two Lipschitz- and those h ∈ ℝnh, with nm + nh = n, that cannot be measured (hidden). continuous  functions  f  and  g,  which  also  depend  on  a  vector  of system in eq. 1 are uniquely determined by the structural parameters structural parameters q ∈ q ⊂ ℝnq. The trajectories m(t) and h(t) of q and by the initial conditions m(0) = m0, h(0) = h0. Here, we assume that some of the quantities q are known, while the others are not known and need to be determined by fitting the trajectories of measurable variables m(t). identify the trajectories, which comprises the unknown terms of q We denote by p ∈ p ⊂ ℝnp, the set of unknown parameters that and the unknown initial conditions h0. The initial values of the hidden variables are not known and act indeed as parameters for the tra- jectories generated by system in eq. 1. The initial conditions of the measurable variables m0 may be considered fitting parameters as well. System in eq. 1 is said to be structurally identifiable when the measured variables satisfy (21)\n(2) m(t,  ˆ p  ) = m(t, p ) , ∀ t ≥  0 ⇒   ˆ p  = p\nfor almost any p ∈ p. Notice that, as a consequence of the existence and uniqueness theorem for the initial value problem, if system in eq. 1 is structurally identifiable, the hidden variables can also be uniquely determined. Structural identifiability guarantees that two different sets of parameters do not lead to the same time course for the measured variables. When this condition is not met, one cannot uniquely associate a data fit to a specific set of parameters or, equivalently, recover the parameters from the measured variables (23).\nAssessing the practical identifiability of a model Structural identifiability, however, is a necessary but not sufficient condition for parameters estimation, so that when it comes to use a dynamical system as a model of a real phenomenon, it is fundamen- tal to quantify the practical identifiability of the dynamical system.\n\nTo do this, we consider a solution,   m ̄  (t ) = m(t,   p ̄  )  and    h ̄  (t ) = h(t,   p ̄  ) , obtained from parameters  p  =    p ̄   , and we explore how much the func- tions m(t) and h(t) change as we vary the parameters    p ̄     by a small amount p. To first order approximation in the perturbation of the parame- ters, we have  m  =   𝛛m _ 𝛛p   p + O(∥ p  ∥   2 )  and  h =   𝛛h _ 𝛛p  p + O(∥  p  ∥   2 ) . Hence,  by  dropping  the  higher  order  terms,  we  have ∞ ∞ ∣ h ∣   2  dt  =   p   T  Hp , ∥ m  ∥   2  =    ∫ 0 ∣ m  ∣   2  dt  =   p   T  Mp  and  ∥ h  ∥   2  =    ∫ 0 where the entries of the sensitivity matrices  M =  M( p ̄   ) ∈   ℝ    n  p  × n  p     and defined as H  =  H( p ̄   ) ∈   ℝ    n  p  × n  p     for the measured and unmeasured variables are\nH  =  H( p ̄   ) ∈   ℝ    n  p  × n  p     for the measured and unmeasured variables are ∞ ∞   ∂  m   T  ─  ∂  h   T  ─ (3) (M)  ij   =    ∫ ∂  p  i       ∂ m ─ ∂  p  j     dt ;  (H)  ij   =    ∫ ∂  p  i       ∂ h ─ ∂  p  j     dt 0 0 Note that these matrices are positive semidefinite by con- struction. The smallest change in the measured variables m(t) will take place if p is aligned along the eigenvector v1 of M correspond- ing to the smallest eigenvalue 1(M). Hence, we can consider    = _    1  (M)    to quantify the sensitivity of the measured variables to the √ parameters.  Practical  identifiability  requires  high  values  of    as these  indicate  cases  where  small  changes  in  the  parameters  may produce considerable variations of the measurable variables, and therefore, the estimation of the model parameters from fitting is more reliable. Suppose now we consider a perturbation, p1, of the parameters aligned along the direction of v1. We can evaluate the change in h(t) due to this perturbation by   p 1  T  H  p  1   ─  (4)     2  =   p 1  T    p  1 The value of  quantifies the sensitivity of the hidden variables to the parameters of the model, when these parameters are estimated from the fitting of the observed variables since ∥h ∥ =  ∥ p1∥. Notice that in this case, and differently from , lower values of  are desirable because they imply a better prediction on the hidden variables. Last, with the help of the sensitivity matrices defined above, we can also evaluate the sensitivity of the hidden variables to the mea- sured variables as\n  p   T  Hp ─    2  =    max  (5) ∥p∥=1   p   T  Mp\nThis parameter is of particular relevance here, since it provides a bound on how the uncertainty on the measured variables affects the evolution of the hidden variables. In addition, the parameter 2 can be efficiently computed as it corresponds to the maximum general- ized  eigenvalue  of  matrices  (H,  M),  as  shown  in  Materials  and Methods. The  sensitivity  matrices  are  useful  in  studying  the  effect  of changing the number of hidden variables and unknown parameters on the practical identifiability of a model. Assume that we have access to one more variable, thus effectively increasing the size of the set of measured variables to nm′ = nm + 1 and, correspondingly, reducing that of the unmeasured variables to nh′ = nh − 1. This cor- responds to considering new variables m´ and h´. From the defini- tion in Eq. 3, the new sensitivity matrix can be written as M′ = M + M1, where M1 is the sensitivity matrix for the newly measured variable. Given Weyl\'s inequality [page 239 of (30)], we have that\n2 of 13\n\n1(M′) ≥ 1(M) + 1(M1) and since M1 is also positive semidefinite, 1(M′) ≥ 1(M). This means that measuring one further variable (or more than one) of the system increases the practical identifiability of a model, as expected. As H′ = H − M1, it is also possible to demon- strate that (M′) ≤ (M) (see Materials and Methods). Let us now consider a different scenario: Suppose we have a priori knowledge of one of the model parameters so that we do not need to estimate its value by fitting the model to the data. In this case, we can define unmeasured variables, respectively. Given the Cauchy\'s interlacing new sensitivity matrices   ˜ M ,  ˜ H  ∈   ℝ   ( n  p  −1)×( n  p  −1)   for the measured and theorem [page 242 of (30)], we have that     1  ( ˜ M  ) ≥    1  (M) , which im- plies that practical identifiability is improved by acquiring a priori in- formation on some of the model parameters. For instance, in the context of COVID-19 models, one may decide to fix some of the parameters,  such  as  the  rate  of  recovery,  to  values  derived  from medical  and  biological  knowledge  (24,  31–33)  and  to  determine from fitting the more elusive parameters, such as the percentage of asymptomatic individuals or the rates of transmission. The sensitivity measures we have introduced point out that prior knowledge of some of the parameters, or a larger set of measurable variables, reduces the sensitivity of the measured variables to the parameters and that of the hidden variables to a variation in the measured ones. However, gathering further knowledge can be diffi- cult or even not possible so that these results, which should not be interpreted as an oversimplified solution to the problem of identifi- ability, have to be considered in the light of practical issues that might arise in the measurement of the model variables and pa- rameters.\nThe sensitivity measures reveal different regimes of identifiability As a first application, we study the practical identifiability of a four compartment mean-field epidemic model (34) in the class of SIAR models (35), developed to assess the impact of asymptomatic carriers of COVID-19 (8, 36, 37) and other diseases (38–40). In such a model (Fig. 1), a susceptible individual (S) can be infected by an infectious individual who can either be symptomatic (I) or asymptomatic (A). The newly infected individual can either be symptomatic (S → I) or asymptomatic (S → A). Furthermore, we also consider the possibility that asymptomatic individuals develop symptoms (A → I), thus accounting for the cases in which an individual can infect before and after the onset of the symptoms (41). Last, we suppose that individuals  cannot  be  reinfected  as  they  acquire  a  permanent immunity (R). One of the crucial aspects of COVID-19 is the presence of asymptomatic individuals who are difficult to trace as the individuals themselves could be unaware about their state. Consequently, we assume that the fraction of asymptomatic individuals, a(t), is not measurable, while the fractions of symptomatic, (t), and recovered, r(t), are measured variables, that is, m ≡ [, r] and h ≡ [s, a]. As mentioned above, practical identifiability is a property of the trajec- tories of the system, which are uniquely determined by the values of the unknown parameters p. Here, we illustrate how the sensitivity of  both  measured  and  unmeasured  variables  changes  with  the probability  that a newly infected individual shows no symptoms when all the other parameters of the model are fixed (to the values reported in Materials and Methods). Concerning the choice of vector p, here, we consider the following case. First, as the number of symptomatic infectious and recovered individuals are supposed\n\n\nFig. 1. Graphical representation of a SIAR model in which infectious individuals can either be symptomatic (I) or asymptomatic (A) (see also Eq. 21 in Materials and Methods).\nto be measurable, we have assumed the initial conditions (0), r(0), and the recovery rate of the symptomatic individuals, i.e., IR,to be known quantities. Second, we assume to be able to measure, for instance, through backward contact tracing, the rate at which asymptomatic individuals develop symptoms, i.e., AI. Hence, the parameters to be determined are the remaining ones, i.e., p = [a(0), I, A, , AR]. Figure 2A shows a nontrivial nonmonotonic dependence of our sensitivity measures,  and , on . The value of  has a peak at  = 0.51, in correspondence of which  takes its minimum value. This represents an optimal condition for practical identifiability, as the sensitivity to parameters of the measured variables is high, while that  of  the  unmeasured  ones  is  low,  and  this  implies  that  the unknown quantities of the system (both the model parameters and the hidden variables) can be estimated with small uncertainty. On the contrary, for  = 0.86, we observe a relatively small value of  and  a  large  value  of  ,  meaning  that  the  measured  variables  are poorly identifiable, and the unmeasured variables are sensitive to a variation of parameters. This is the worst situation in which the estimated parameters may substantially differ from the real values, and the hidden variables may experience large variations even for small  changes  in  the  parameters.  Furthermore,  the  quantity  , which  measures  the  sensitivity  of  the  hidden  variables  to  the measured ones, reported in Fig. 2B, exhibits a large peak at the value of  for which  is minimal. This is due to the fact that the vector that determines  is almost aligned with v1. When this holds, we have that  = η/, which explains the presence of the spike in the  curve. Similarly, the sensitivity   takes its minimum almost in correspondence of the maximum of . The behavior of the model for  = 0.86 is further illustrated in Fig. 2C, where the trajectories obtained in correspondence to the unperturbed values of the parameters, i.e., m(t, p) and h(t, p) (solid lines), are compared with the dynamics observed when p undergoes a perturbation with ∥p∥ = 0.3∥p∥ along v1 (dashed lines). The small sensitivity  of the measured variables (t, p) and r(t, p) to parameters is reflected into perturbed trajectories that remain close to the unperturbed ones, whereas the large sensitivity  of the unmeasured variables s(t, p) and a(t,  p) yields perturbed trajectories that significantly deviate from the unperturbed ones. We now illustrate the different levels of identifiability that appear in the SIAR model for diverse settings of the parameters. Its analysis, in fact, fully depicts the more complete perspective on the\n3 of 13\n\nFig. 2. Practical identifiability of the SIAR model in Fig. 1 as a function of the fraction  of asymptomatic new infectious individuals. (A) Sensitivity  and  of measured and hidden variables, respectively, to the parameters of the model. (B) Sensitivity  of the hidden variables to the measured ones. (C) State variables for unper- turbed values of parameters (with  = 0.86, solid line) and for a perturbation with ∥p ∥ = 0.3 ∥ p∥ along the first eigenvector of M (dashed lines). problem of practical identifiability offered by simultaneously inspecting the sensitivity measures,  and . As the two sensitivity measures are not necessarily correlated, there can be cases for which a high identifiability of the measured variables to the parameters, i.e., large values of , corresponds to either a low or a high identifi- ability of the hidden variables to the parameters. Analogously, for other system configurations, in correspondence of small values of , namely, to nonidentifiable parameters, one may find large values of , meaning that the hidden variables are nonidentifiable as well or, on the contrary, small values of , indicating that the hidden variables are poorly sensitive to parameter perturbations. Together, four distinct scenarios of identifiability can occur, and all of them effectively appear in the SIAR model (Fig. 3): (A) low identifiability of the model parameters p and high identifiability of the hidden variables h, (B) high identifiability of both p and h, (C) low identifiability of both p and h, and (D) high identifiability of p and low identifiability of h. To illustrate them, we have considered four distinct configura- tions of the model (with parameters as given in Table 3 and illus- trated in Materials and Methods) and, for each case study, compared the unperturbed trajectories to the perturbed ones, with the vector of parameters undergoing a variation ∥p ∥ = 0.3 ∥ p∥ along v1. Fig. 3. Four scenarios of identifiability for the SIAR model of Fig. 1. All panels As regard cases (A) and (C), we have considered the vector of param- show the system dynamics (solid line) and the evolution of the system when the eters to determine to be p = [(t), a(0), r(t), I, A, , IR, AR, AI], vector of parameters undergoes a variation p such that ∥p ∥ = 0.3 ∥ p∥ along the while for the cases (B) and (D), we have p = [a(0), I, A, , AR], first eigenvector of M (dashed lines). (A) and (C) display configurations for which which is the same choice of p adopted in Fig. 2. Figure 3 shows the the observed variables (, r) are not sensitive to the variation, i.e., the model parame- results obtained for each parameter configuration. In each panel, ters are not identifiable, while (B) and (D) show the opposite case. Furthermore, (A) the  solid  lines  represent  the  unperturbed  trajectories,  while  the and (B) present scenarios for which the unobserved variables (s, a) are insensitive dashed lines correspond to the perturbed dynamics. In cases (A) and to the variation, meaning that they are predictable; vice-versa, (C) and (D) show the case in which the variables s and a are sensitive. (B), we see that, under the variation p, the perturbed trajectories of 4 of 13\n\nthe hidden variables remain close to the unperturbed dynamics. Hence, the hidden variables are highly identifiable. Conversely, in cases (C) and (D), the perturbed trajectories substantially differ from the unperturbed dynamics, meaning that the hidden variables are poorly identifiable as they are sensitive to a variation of the model parameters. As concerns the measured variables, in cases (A) and (C), the perturbed trajectories slightly differ from the unperturbed dynamics. Therefore, as the measured variables are insensitive to the  perturbation  p,  the  model  parameters  have  a  low  degree  of identifiability. On the other hand, in cases (B) and (D), the perturba- tion  of  the  parameters  significantly  affects  the  trajectories  of  the\nTable 1. Values of , , and  for the four configurations of the SIAR model shown in Fig. 3. Case A Case B Case C Case D  0.0096 0.15 0.013 0.091  0.012 0.16 0.36 1.4  34 5.2 29 15\n\nfrom data. In these conditions, a small uncertainty in the measurable variables due to the presence of noise in the data can propagate\n\nPoorly identifiable models may provide unreliable predictions when the parameters are estimated from data So far, we have illustrated how variations on the parameters affect the trajectories of measurable and hidden variables under different degrees of identifiability. However, a high sensitivity of the hidden variables to measured ones has relevant practical consequences, especially when the parameters are unknown and need to be fitted from data. In these conditions, a small uncertainty in the measurable variables due to the presence of noise in the data can propagate markedly and make the prediction of the hidden variables unreliable. Hence, in this section, we study how the lack of practical identifiability\nmeasured variables, meaning that the set of parameters reproducing the observed data is more identifiable. Last, Table 1 illustrates the values of the sensitivity measures , , and  for each case. In particular, case (C) represents the worst scenario as the value of  is relatively small, meaning that the model parameters p are poorly identifiable, and the value of  is large, indicating a high sensitivity of the hidden variables to the parameters. Conversely, the best scenario is represented by case (B), for which both  the  model  parameters  and  the  hidden  variables  are  highly identifiable as the value of  is large compared to the other cases, while the value of  remains relatively small.\nFig. 4. Dynamics of the SIAR model when two different methods to estimate the model parameters from data are used. (A to D) display the results obtained by a least square error minimization procedure, while (E to H) show the outcome of Bayesian inference. The time evolution of both measured and hidden variables (solid lines) is reported together with the data. Data are shown with different markers if they pertain to measured variables (full circles, used for fitting) or unmeasured ones (empty circles, not used for fitting).\n5 of 13\n\n\nFig. 5. Graphical representation of a nine-compartment model for the propagation of COVID-19 (see also Eq. 24 in Materials and Methods).\ncan affect the predictions of the SIAR model when this is fitted to empirical data. The reliability of the model predictions has been investigated  by  means  of  two  different  fitting  techniques:  a  least square error minimization procedure (42), which provides a point estimate of the parameters, and a Bayesian inference approach (43), which, conversely, gives an estimate of the probability distribution in the parameter space. To carry out the numerical analysis, we consider the model under the same settings (i.e., fixing the values of the six parameters and the initial values of three variables) as those adopted in case (C) in the previous section, which correspond to the case of low identifiability of both the parameters and the hidden variables. We then generate from such a model a synthetic dataset of trajectories, which we fit using the two approaches mentioned above (see Materials and Methods for further details). All the model parameters  are  considered  unknown  and  thus  need  to  be  deter- mined through the fit. First, we consider the least square error minimization approach. To show how, because of the lack of identifiability of the model, significant variations in the dynamics of the hidden variables can be obtained when fitting the measured variables, we have performed the  following  analysis.  As  the  estimation  procedure  (based  on  a nonlinear optimization algorithm; see Materials and Methods) starts from an initial guess of the fitting parameters, indicated as p0, instead of fitting a single set of values, we have repeated the proce- dure under the very same conditions of the algorithm, for 500 runs, randomly selecting p0 from a Gaussian distribution centered on a fixed point of the parameter space and with variance equal to 0.25. We then discarded those runs yielding a fitting error d > 0.015, which corresponds to a relative error of 2.5%, thus keeping a total of 65 sets of parameters fitting the measured variables with a similar value of the error. The fact that different sets of parameters are obtained in this way may indicate that the error function has several local minima. Figure 4 (A to D) displays the average trajectories (over the 65 sets of parameters) of the four state variables (solid lines) and the respective regions where 95% of the trajectories lie (shadowed  area).  While  the  dynamics  of  the  measured  variables (Fig. 4, C and D) produced by the SIAR model are in very good agreement with the data, the same is not true for the temporal evolution of the hidden variables (Fig. 4, A and B). In particular, the\n\nnumber of newly asymptomatic infected individuals is substantially overestimated. We now consider the Bayesian inference approach. To imple- ment it, we used the Delayed Rejection Adaptive Metropolis (DRAM) (44), a Markov Chain Monte Carlo (MCMC) algorithm, with set- tings as described in Materials and Methods. In particular, since we have here assumed to have no a priori information on the value of the model parameters, we have considered uniform prior probability distributions, representing the less informative conditions for the model (further details are given in Materials and Methods). Figure 4 (E to H) shows the temporal evolution of the SIAR variables. Solid lines represent the average trajectory obtained by sampling 500 sets of parameters from the posterior distributions, while the shadowed areas indicate the regions where 95% of the trajectories lie. Similarly, to the case of the least square minimization, while the dynamics of the measured variables (Fig. 4, G and H) is in a good agreement  with  the  synthetic  data,  the  prediction  of  the  hidden variables (Fig. 4, E and F) is not. At variance with the previous example, the number of newly asymptomatic infected individuals is here largely underestimated. Hence, these results indicate that the lack  of  identifiability  can  lead  to  unreliable  results  even  when  a Bayesian approach is adopted. In  the  analysis  presented  in  this  section,  we  have  assumed  to have no a priori information on the values of the model parameters. Therefore, when performing the least square error minimization, we have extracted all the parameters from fitting, while, following the same reasoning, we have chosen a uniform prior probability distribution in the Bayesian approach. When instead we have strong a priori knowledge of a disease, this can be used to inform the models, by fixing the values of certain parameters while estimating the others, in the case of the least square error method, or by considering  more  informative  prior  distributions,  in  the  case  of Bayesian inference. When a priori information on the values of the parameters  can  be  obtained,  for  instance,  through  medical  and biological studies, the model predictions are expected to become less affected by uncertainty. The analysis of the sensitivity matrices (see also Materials and Methods) confirms this expectation as we have demonstrated that additional knowledge of the parameters, or\n6 of 13\n\n\n\n\n\nFig. 6. Modeling the COVID-19 outbreak in Italy. The evolution of both measured (A to D) and hidden variables (E to H) of the model in Fig. 5 (solid lines) is reported together with the official data from the Civil Protection Department (circles). (D) individuals. Following the study of Giordano et al. (16), to account the measurement of a hidden variable, can reduce the sensitivity to for the different nonpharmaceutical interventions and testing strat- the measured variables, thus improving the reliability of the predic- egies issued during the COVID-19 outbreak in Italy (47, 48), the tion. However, there are cases in which a priori knowledge is not model parameters have been considered piece-wise constant and available, and the analysis of identifiability becomes crucial. As an estimated  using  nonlinear  optimization  by  fitting  of  the  official example, one could estimate the percentage of asymptomatic indi- data provided by the Civil Protection Department (49). As the data- viduals according to serological surveys or to longitudinal studies. set provides the evolution in time of the daily number of home iso- However, these data can be unavailable at an early stage of an lated,  hospitalized,  detected  recovered,  and  deceased  individuals, epidemic outbreak (45, 46), preventing their use to inform the we have considered four measured and five hidden variables in epidemiological models. These considerations hallmark once again the need for a synergistic approach to study newly discovered infec- the model, namely, m ≡ [H, T, Rd, D] and h ≡ [S, E, IA, IS, Ru]. It is here worth discussing an important issue that concerns the tious diseases and stress the importance of assessing the reliability model parameters. We have considered that different policy strategies of mathematical modeling when the amount of available informa- affect, according to their nature, only specific parameters. In particu- tion is limited. lar, we have assumed that a change in the containment strategy Lack of identifiability in COVID-19 modeling prevents leads to a variation in the transmission rates , while an adjustment reliable predictions in the testing strategy affects the values of the parameters ISH, HT, As a second application, we show the relevance of the problem of and HRd (further details are reported in Materials and Methods). As both the containment and the testing strategies in Italy have practical identifiability in the context of COVID-19 pandemic frequently changed during the pandemic, most of the parameters to modeling. We consider a realistic model (Fig. 5) of the disease estimate consist of transmission and detection rates. While other propagation, that is a variant of the SIDARTHE model (16) and is parameters, such as the death or the recovery rates, can be derived characterized by nine compartments accounting respectively for from the current literature (50), very limited information is available susceptible (S), exposed (E), undetected asymptomatic (IA), un- on the transmission and detection rates that are difficult to measure detected symptomatic (IS), home isolated (H), treated in hospital (T), undetected recovered (Ru), detected recovered (Rd), and deceased directly  and,  therefore,  need  to  be  estimated  by  fitting  available\n\n7 of 13\n\ndata. Hence, as in (16), we have assumed all the model parameters to be unknown. To show how significant variations in the evolution of the hidden variables can arise when fitting the measured variables, we have performed a numerical analysis similar to the one of the previous section. Again, rather than fitting a single set of values, we have repeated the minimization procedure under the same conditions of the algorithm, for 500 runs, randomly selecting the initial guess p0 from a Gaussian distribution centered on a fixed point of the pa- rameter space, with variance equal to 0.25. We discarded the runs yielding a fitting error e > 900, corresponding to a relative error of 1.4%, thus keeping a total of 40 sets of parameters. Figure 6 shows the dynamical trajectories that we have obtained for each of the 40 sets of parameters (solid lines). Both measured (Fig. 6, A to D) and hidden (Fig. 6, E to H) variables are reported. While the time evolution of the measured variables produced by the model is in very good agreement with the empirical data, reported as circles in Fig. 6, significant differences in the trend of the hidden variables appear. A large variability is observed, confirming that the lack of identifiability yields a high sensitivity of the hidden variables to the measured one. These findings have relevant implications. The large uncertainty on the size of the asymptomatic population makes questionable the use of the model as a tool to decide the policies to adopt.\nDISCUSSION The practical identifiability of a dynamical model is a critical, but often neglected, issue in determining the reliability of its predic- tions. In this paper, we have introduced a novel framework to quantify: (i) the sensitivity of the dynamical variables of a given model to its parameters, even in the presence of variables that are difficult to access empirically and (ii) how changes in the measured variables affect the evolution of the unmeasured ones. The measures we have proposed are easy to compute and enable to assess, for instance, if and when the model predictions on the unmeasured variables are reliable or not, even in the cases in which the parameters of the model can be fitted with high accuracy from the available data. As we have shown with a series of case studies, practical identifi- ability can critically affect the predictions of even very refined epidemic models introduced for the description of COVID-19, where dynamical variables, such as the population of asymptomatic individuals, are impossible or difficult to measure. This by no means should question the importance of these models—in that they enable a scenario analysis, otherwise impossible to carry out, and a deeper understanding of the spreading mechanisms of a novel disease—but should hallmark the relevance of a critical analysis of the results that takes into account sensitivity measures. It also high- lights the importance of cross-disciplinary efforts that can provide a priori information on some of the parameters, ultimately improv- ing the reliability of a model (8, 24). A problem related to the one studied in our paper is that of observability,  which  investigates  how  to  reconstruct  the  internal state of a system from measurements on the input and output, under the hypothesis that the model and its parameters are known (51, 52). Techniques based on the observability problem are clearly extremely important and may be applied, for instance, to derive the time evolution of asymptomatic individuals from measurements on\n\ninfected and recovered individuals, when it is possible to develop a fully observable model with known parameters.\nMATERIALS AND METHODS The sensitivity matrices and their properties The sensitivity matrices considered in this paper are given by\n∞ ∞  ∂ m   T  ─  (6) M  ij   =    ∫ 0 ∂ p  i       ∂m ─ ∂ p  j     dt ;  H  ij   =    ∫ ∂ h   T  ─ ∂ p  i       ∂h ─ ∂ p  j     dt 0\nwhere the vector functions m = m(t, p) and h = h(t, p) are obtained integrating system in eq. 1. The derivative of measurable and hidden variables with respect to the parameters p\nm  i   ≡  ∂ m / ∂ p  i  ,  h  i   ≡  ∂ h / ∂ p  i\ncan be obtained by integrating the system\n= ∂ f ─ ∂ m   ·  m  i   +   ∂ f ─ ∂ h   ·  h  i   +   ∂ f ─ ∂  p  i d  m  i   ─ dt           (7) = ∂ g ─ ∂ m   ·  m  i   +   ∂ g ─ ∂ h   ·  h  i   +   ∂ g ─ ∂  p  i d  h  i   ─ dt where i = 1, …np. The numerical evaluation of the sensitivity matrices is carried out first by integrating system in eq. 7 (for this step we use a fourth-order Runge-Kutta solver with adaptive step size control), resampling the trajectories with a sampling period of 1 day, and then performing a discrete summation over the sampled trajectories. Moreover, inte- gration is carried out over a finite time interval [0,  ], with large enough . In the context of our work, as we have considered SIR (susceptible infected removed)-like epidemic models, we set the value of  such that the system has reached a stationary state, i.e., the epidemic outbreak has ended, as every infected individual has eventually recovered (or dead, depending on the model). We now present an important property of the sensitivity matri- ces. We will only take into account the set of measured variables m, as similar considerations can be made for the hidden variables. Let us assume to be able to measure only a single variable, so that the vector m collapses into a scalar function, which we call m1(t). In this case, the element Mij of the sensitivity matrix would be simply given by\n∞  (8) (M)  ij   =    ∫ ∂ m  1   ─ ∂ p  i       ∂ m  1   ─ ∂ p  j     dt 0\nLet us call this sensitivity matrix M1. Consider now a larger set of measured variables m = (m1, m2, …, mnm). The quantity ∂mT/∂pi∂m/∂pj in Eq. 6 is given by\n∂ m   T  ─ (9) ∂ p  i       ∂m ─ ∂ p  j     =    ∂ m  1   ─ ∂ p  i       ∂ m  1   ─ ∂ p  j     +   ∂ m  2   ─ ∂ p  i       ∂ m  2   ─ ∂ p  j     + … +   ∂ m   n  m     ─ ∂ p  i        ∂ m   n  m     ─ ∂ p  j\nTherefore, integrating over time in the interval [0, ∞ ] and given the linearity property of the integrals, we find that the sensitivity matrix M of the set of the measured variables is given by the sum of the sensitivity matrices of the single measured variables. Formally, we have that\n8 of 13\n\n(10) M =   M  1   +  M  2   + … +  M   n  m\nThis property of the sensitivity matrices is useful to demonstrat- ing how measuring a further variable affects the sensitivity mea- sures    and  ,  as  discussed  in  the  following  subsection  and  in Results. Last, because matrices M and H are positive semidefinite, their eigenvalues are nonnegative. For any positive semidefinite matrix A of order m, we shall denote its eigenvalues as 0 ≤ 1(A) ≤ 2(A) ≤ … ≤ m(A).\nSensitivity measures and their properties Here, we discuss in more detail the sensitivity measures introduced in Results. First, we want to propose a measure to quantify the practical identifiability of the model parameters given the measured variables. To do this, we need to evaluate the sensitivity of the trajectories of the measured variables to a variation of the model parameters. If this sensitivity is small, then different sets of parame- ters will produce very similar trajectories of the measured variables, meaning that the parameters themselves are poorly identifiable. In particular,  as  a  measure  of  the  parameters  identifiability,  we  can consider the worst scenario, namely, the case in which the perturba- tion of the parameters minimizes the change in the measured vari- ables. This happens when the variation of the model parameters p is aligned along the eigenvector v1 of M corresponding to the minimum eigenvalue 1(M). Given the definition of M, we have that _    1  (M)   ∥  p ∥ ; hence, we can consider the quantity ∥ m  ∥ =  √\n_  (11)   1  (M)   =   √\nas an estimate of the sensitivity of the measured variables to the parameters. Note that, here and in the rest of the paper, ∥v∥ denotes the Euclidean norm of a finite dimensional vector v, ∥v∥2 = v · v, while for a function u(t), ∥u∥ denotes the L2 norm of u in [0, ∞ ], i.e., ∥ u  ∥   2  =   ∫0  ∞    u · u dt . Let us now focus on the hidden variables h. In general, as the hidden variables are not directly associated to empirical data, the largest uncertainty on the hidden variables is obtained in corre- spondence of a variation of the parameters along the eigenvector of H associated to the largest eigenvalue, namely,      n  p     (H). Hence, to quantify the sensitivity of the hidden variables to the parameters, one may consider\n_  (12)    n  p    (H)   MAX    =   √\nHowever, it is crucial to note that the hidden variables ultimately depend on the parameters of the model, which are estimated by fitting data that are available for the measured variables only. As a consequence, it is reasonable to consider a quantity that evaluates how the uncertainty on the model parameters (determined by the uncertainty of the measured variables and by their sensitivity to the parameters) affects the identifiability of the hidden variables. There- fore, as a measure of the sensitivity of the hidden variables to the parameters, we consider\n  p 1  T  H  p  1   ─  (13)    2  =   p 1  T    p  1\n\nwhere p1 is a perturbation of the parameters along the eigenvector v1  of  M  corresponding  to  the  minimum  eigenvalue  1(M).  Note that, when v1 and the eigenvector of H corresponding to the largest eigenvalue      n  p     (H) are aligned, by definition, we have  = MAX. Last, we want to define a quantity to estimate how much the hidden variables are perturbed given a variation of the measured ones. In particular, as a measure of the sensitivity of the hidden variables to the measured variables, we consider the maximum perturbation of the hidden variables given the minimum variation of the measured ones, which is   p   T  Hp ─     2  =    max  (14) ∥p∥=1   p   T  Mp Note that 2 can be computed considering the following gener- alized eigenvalue problem\nwhere H and M are the sensitivity matrices for the hidden and the observed variables respectively, and k = k(M, H) denotes the k-th generalized eigenvalue of matrices M and H. We will denote by      n  p the largest generalized eigenvalue and u the corresponding general- ized eigenvector. Note that, since both matrices are symmetric, if u is a right eigenvector, then uT is a left eigenvector. Multiplying each member of the equation by uT and dividing by uTMu, we obtain  =    max  (16)    n  p     =     u   T  Hu ─ ∥v∥=1      v   T  Hv ─ u   T  Mu v   T  Mv where one can recognize the definition of 2 provided in Eq. 14. In other words, 2 represents the largest eigenvalue of the matrix M−1H. It is worth noting two aspects about the sensitivity measure . First, given the definitions in Eqs. 11 and 12, for any p with ∥p∥ = 1, we 2 and pTMp ≥ 2. As a consequence, have that    p   T  Hp  ≤    MAX we have that 2  (17) ─     2  ≤      MAX    2\nSecond, when the vector p that determines  is aligned with the eigenvector v1 of M, it is possible to express  in terms of the sensi- tivity measures  and . When p = ∥ p ∥ v1 = v1, recalling defini- tions in Eqs. 11 and 13, one obtains   v 1  T  M  v  1   =      2  , while   v 1  T  H  v  1   =      2  , from which it follows\nIn addition, we note that if v1 and the eigenvector of H correspond- ing to its largest eigenvalue are aligned, one obtains that  = MAX/, which is the maximum value for the sensitivity measure . We now demonstrate that the sensitivity of the hidden variables to the measured ones, 2, decreases as we measure one further vari- able. Let us assume now that we are able to measure one further variable, thus increasing the size of the set of measured variables to nm′ = nm + 1 and, correspondingly, reducing that of the unmeasured variables to nh′ = nh − 1. Given the property in Eq. 10, the new sensitivity matrices can be written as M′ = M + M1 and H′ = H − M1, where by M1, we denote the sensitivity matrix for the newly mea- sured variable. The new generalized eigenvalue problem is\n(15) H u  k   =     k   M u  k\n(18)   =     ─ \n9 of 13\n\n(19) H′u′= ′M′u′ ⇔  (H −  M  1   ) u′= ′(M +  M  1   ) u′\nwhere, for simplicity, we have denoted by ′ the largest generalized eigenvalue of matrices M′ and H′. Left multiplying by u′T and dividing by u′TMu′, we obtain\n───────────   ≥     u′   T  Hu′ ─ =     u′   T  H\'u′+  u′   T   M  1   u′ ≥ λ   n  p      =     u   T  Hu ─ u   T  Mu u′   T  Mu′ u′   T  Mu′− u′T  M  1   u′     (20)  u′   T  H\'u′ ─ =  λ′ u′   T   M    ᾽  u′\nwhere the first inequality comes from the definition of      n  p     , while the second comes from the fact that H, M, H′, M′, and M1 are positive semidefinite. In short, we find that      n  p      ≥ ′, meaning that, by mea- suring one variable, the sensitivity of the hidden variables to the measured ones decreases.\nSIAR model and setup for numerical analysis The SIAR model of Fig. 1 is described by the following equations\n⎧ s ̇    =  − s(   I    +    A   a) ⎪           (21)   ̇    =  (1 −  ) s(   I    +    A   a ) +    AI   a −    IR       ⎨ ⎪ a ̇    =  s(   I    +    A   a ) − (   AI   +    AR   ) a ⎩ r ̇    =     IR    +    AR   a\nwhere s(t), (t), a(t), and r(t) represent population densities, i.e., s(t) = S(t)/N, (t) = I(t)/N, a(t) = A(t)/N, and r(t) = R(t)/N, where S(t), I(t), A(t), and R(t) represent the number of susceptible, infec- tious, asymptomatic, and recovered individuals, and N is the size of the population, so that s(t) + (t) + a(t) + r(t) = 1. Here, I and A are the transmission rates for the symptomatic and the asymptomatic individuals, respectively,  is the probability for newly infected indi- viduals to show no symptoms, AI is the rate at which asymptomatic individuals become symptomatic, and IR and AR are the recovery rates for the two infectious populations. Note that all these parame- ters are positive quantities. Asymptomatic individuals are difficult to trace as the individuals themselves could be unaware about their state. As a consequence, we  assume  that  the  density  of  asymptomatic  individuals  is  not measurable, while the densities of symptomatic and recovered indi- viduals  are  measured  variables.  According  to  the  notation  intro- duced in Eq. 1, we therefore have that m ≡ [, r] and h ≡ [s, a]. Note that, as a first approximation, here, we assume to be able to trace the asymptomatic individuals once they recover. The results presented in Fig. 2 have been obtained considering the following setup. As the number of symptomatic infectious and recovered individuals are considered measurable, we have assumed that the initial conditions (0), r(0), and the rate of recovery IR are known parameters. Second, we have supposed to be able to measure, for instance, through backward contact tracing the rate at which asymptomatic individuals develop symptoms, i.e., AI. Hence, the vector of parameters to determine by calibrating the model is given by p = [a(0), I, A, , AR]. Table 2 displays the value of the model parameters used to obtain the results shown in Fig. 2. For the analysis of the four scenarios considered in Fig. 3, the values of the model parameters have been set as given in Table 3. Furthermore, to better contrast the results arising in the different case studies, in (A) and (C), we have considered p = [(0), a(0),\n\nTable 2. Values of the model parameters used for the case study in Fig. 2. 0 a0 r0 I A IR AR AI 0.05 0.1 0 0.6 0.3 0.1 0.2 0.03\nTable 3. Values of the model parameters used for the case study in Fig. 3.  0 a0 r0 I A IR AR AI Case A 0.1 0.2 0.05 0.3 0.4 0.26 0.1 0.2 0.03 Case B 0.05 0.1 0 0.6 0.3 0.51 0.1 0.2 0.03 Case C 0.1 0.2 0.05 0.6 0.8 0.77 0.1 0.2 0.1 Case D 0.1 0.2 0.05 0.3 0.4 0.53 0.1 0.2 0.03\nr(0), I, A, , IR, AR, AI], while in (B) and (D), we have set p = [a(0), I, A, , AR].\nEstimating the parameters of the SIAR model The SIAR model parameters have been estimated from data by adopting two different approaches, i.e., a nonlinear least square error minimization and a Bayesian inference. To generate a synthetic dataset, we have integrated the deterministic model in Eq. 21. To mimic measurement errors, we have adopted the following proce- dure. First, we compute   r ̄  (t)  by adding to r(t) a uniform noise in the interval (− t/2, t/2), where t = ∣r(t + 1) − r(t)∣, checking that the synthetic time series remains monotonically nondecreasing. We have then generated the data   s ̄  (t)  in a similar fashion, this time controlling  that  the  synthetic  time  series  remains  monotonically nonincreasing. To generate the data    ̄  (t) , we have added a Gaussian noise with zero mean and standard deviation (SD) equals to 3% to the time series, making sure that   s ̄  (t ) +   ̄  (t ) +  r ̄  (t ) ≤ 1 . Last, the data   a ̄  (t) have been evaluated using the fact that   s ̄  (t ) +   ̄  (t ) +  a ̄  (t ) +  r ̄  (t ) = 1 . The integration of Eq. 21 has been carried out by using the lsoda ordinary differential equation (ODE) solver (53, 54) and then resa- mpling the data with a sampling period of one time unit. We assumed that the density of asymptomatic individuals a is not measurable, while the densities of symptomatic and recovered individuals, i.e.,  and r, are measured variables. As regard to the least square error minimization approach, the model parameters have been estimated using a nonlinear optimiza- tion procedure (implemented via the function fmincon in MATLAB) with the following objective function to minimize ______________________________      (22)  1 ─ 2     ∑ (     ((k ) −  _  (k ) )   2  +  (r(k ) −   _ r  (k ) )   2  ) k=1 d  =   √ where    ̄  (k)  and   r ̄  (k)  with k = 1, …,  (with  = 50) represent the noisy synthetic time series of the densities of infectious and recovered individuals, respectively, while  (k) and r(k) are the values of the corresponding variables obtained from the integration of Eq. 21. The core idea of Bayesian inference is to provide an a posteriori probability distribution for the model parameter vector, p, given an a priori probability distribution on the value of p and a likelihood function, which quantifies the goodness of a model in reproducing empirical data D. The relationship between these is given by the Bayes\' theorem, which reads\n\n10 of 13\n(p  ∣  D ) =    ℒ(D  ∣ p ) (p) ∫ p     ℒ(D  ∣  p ) (p ) dp\n────────────   (23) (p  ∣  D ) =    ℒ(D  ∣ p ) (p) ∫ p     ℒ(D  ∣  p ) (p ) dp\n(p  ∣  D ) =    ℒ(D  ∣ p ) (p) ∫ p     ℒ(D  ∣  p ) (p ) dp where (p) indicates the prior distribution, ℒ(D∣p) the likelihood, and (p∣D) the posterior distribution. Usually, it is not possible to evaluate analytically the integral appearing in the denominator, es- pecially when a large number of parameters are considered. There- fore, one relies on MCMC algorithms, which allow one to approximate of the posterior distribution. The MCMC algorithm we used to implement the Bayesian inference is the DRAM (44). As the likeli- hood function ℒ(D∣p), we have considered the root mean square error, evaluated on the measurable variables only, i.e. (t) and r(t), which corresponds to Eq. 22, namely, to the objective function of the nonlinear optimization procedure. As we have assumed to have no a priori knowledge of the values of the model parameters, for the Bayesian inference, we have considered uniform prior probability distributions, which are the simplest and least informative choice (55, 56). Flat priors do not require any additional information apart from setting the interval of possible parameter values. These inter- vals have been defined taking into account the following consider- ations.  On  the  one  hand,  we  have  the  initial  conditions  of  the dynamical variables. As in the SIAR model, these represent popula- tion  densities,  we  can  assume  the  uniform  prior  distribution  for their initial conditions to be defined in the interval [0,1]. Similarly, the parameter , which indicates the fraction of newly infected indi- viduals not developing symptoms, can be assumed to be defined in the same interval. As regard the remaining parameters, since we have assumed to not have any other information except for the fact that they are positive quantities, we can consider the uniform distri- bution to be extended in the interval [0, ∞ ].\nNine-compartment model for COVID-19 The nine-compartment model of Fig. 5 can be considered as a variant of the SIDARTHE model (16). It is characterized by the presence of an incubation state, in which the individuals have been exposed to the virus (E) but are not yet infectious, and by infectious individuals, that, in addition to being symptomatic or asymptomatic, can be either detected or undetected. The model, therefore, includes four classes of infectious individuals: undetected asymptomatic (IA), undetected symptomatic and pauci-symptomatic (IS), home isolated (H, corre- sponding to detected asymptomatic and pauci-symptomatic), and treated in hospital (T, corresponding to detected symptomatic). Last, removed individuals can be undetected (Ru), detected (Rd), or deceased (D). The model dynamics is described by the following equations ⎪\n⎧ S ̇   = − S(    I  A      I  A   +     I  S      I  S   +    H   H +    T   T ) / N E ̇    =  S(    I  A      I  A   +     I  S      I  S   +    H   H +    T   T ) / N − (    EI  A     +     EI  S     ) E I ̇    A    =      EI  A     E − (    I  A   I  S     +     I  A   R   u    )  I  A   −   I  A I  S   ˙    =      EI  S     E +     I  A   I  S      I  A ⎪                       − (    I  S  H   +     I  S  T   +     I  S   R   u    +     I  S  D   )  I  S    (24)      H ̇    =      I  S  H    I  S   +   I  A   − (   HT   +     HR   d    ) H T ̇    =      I  S  T    I  S   +    HT   H − (   T  R   d    +    TD   ) T R ̇     u   =      I  A   R   u     I  A   +     I  S   R   u     I  S R ̇     d   =      HR   d    H +     TR   d    T ⎨ ⎪ ⎩ D ̇    =      I  S  D    I  S   +    TD   T\n\nwhere  the  state  variables  represent  the  number  of  individuals  in each compartment, N = 60 · 106 and S + E + IA + IS + H + T + Ru + Rd + D = N. The official data on the spreading of COVID-19 in Italy made available by the Civil Protection Department [Dipartimento della Protezione Civile, (49)] provide information only on four of the nine compartments of the model, namely, the home isolated (H), hospitalized (T), detected recovered (Rd), and deceased indi- viduals (D). These compartments constitute the set of the measured variables, while the other variables have to be considered as hidden, i.e., m ≡ [H, T, Rd, D] and h ≡ [S, E, IA, IS, Ru]. All the parameters appearing in (24) are considered unknown; thus, they need to be determined through fitting the model to the available data. It should also be noted that, as many nonpharmaceu- tical interventions have been issued/lifted, and the testing strategy has been changed several times over the course of the epidemics (47, 48), not all parameters can be considered constant in the whole period used for the fitting. Hence, similarly to (16), we have divided the whole period of investigation (which in our case ranges from 24 February to 06 July 2020) into different windows, within each of which the parameters are assumed to be constant. In each time window,  one  allows  only  some  parameters  to  vary  according  to what is reasonable to assume will be influenced by the government intervention during that time window. We distinguish two kinds of events that may require an adapta- tion of the model parameters. On the one hand, there are the non- pharmaceutical containment policies aimed at reducing the disease transmission. When these interventions are issued, the value of the parameters    may  vary.  On  the  other  hand,  the  testing  strategy, which affects the probability of detecting infected individuals, was also not uniform in the investigated period. When the testing policy changes, the value of the parameters      I  S  H   , HT, and      HR   d     may vary. Here, we notice two important points. First, the value of      I  S  T    is assumed to be constant in the whole period, as we suppose that there are no changes in how the symptomatic individuals requiring hospitalization are detected. Second, as a change in the sole param- eter      I  S  H    would affect too much the average time an individual remains infected, then HT and      HR   d     also have to be included in the set of parameters that may change. On the basis of these consider- ations, the intervals in which each parameter remains constant or may  change  are  identified.  This  defines  the  specific  piece-wise waveform assumed for each of the parameters appearing in the model and, consequently, the effective number of values that need to be estimated for each parameter. Hereafter,  we  summarize  the  events  defining  the  different windows in which the whole period of investigation is partitioned: 1) On 02 March, a policy limiting screening only to symptomatic individuals is introduced. 2) On 12 March, a partial lockdown is issued. 3)  On  18  March,  a  stricter  lockdown,  which  further  limits nonessential activities, is imposed. 4) On 29 March, a wider testing campaign is launched. Starting from this date, as the number of tests has constantly increased while the number of new infections was decreasing, the parameters are allowed to change every 14 or 28 days, namely, on 11 April, 25 April, and 23 May. 5) On 04 May, a partial lockdown lift is proclaimed. 6) On 18 May, further restrictions are relaxed. 7) On 03 June, interregional mobility is allowed. This is the last time the model parameters are changed.\n11 of 13\n\nNote that, for the time period until 5 April, we have followed the same time partition used in (16). The model parameters have been estimated using a nonlinear optimization procedure (implemented via the function fmincon in MATLAB) with the following objective function to minimize e =                                 ______________________________________________________________ _ ( (H(k) −   _ H  (k ) )   2  +  (T(k ) − T  (k ) )   2  +  ( R   d (k ) −    _ R     d (k ) )   2  +  (D(k ) −   _ D  (k ) )   2 )\ne =                                 ______________________________________________________________ τ _     ( (H(k) −   _ H  (k ) )   2  +  (T(k ) −    T  (k ) )   2  +  ( R   d (k ) −    _ R     d (k ) )   2  +  (D(k ) −   _ D  (k ) )   2 )    1 ─ 4τ     ∑ k=1 (25) √ where   H ̄  (k) ,   T ̄  (k) ,    R ̄     d (k) , and   D ̄  (k)  with k = 1, …,  ( = 134 days) represent the time series of daily data for isolated, hospitalized, detected recovered, and deceased individuals provided by the Civil Protection Department (49), and H(k), T(k), Rd(k), and D(k) are the values of the corresponding variables obtained from the integration of Eq. 24. The integration of Eq. 24 has been carried out by using a suitable ODE solver with maximum integration step size equal to 10−2 days and then resampling the data with a sampling period of 1 day.\nREFERENCES AND NOTES 1.  R. M. Anderson, H. Heesterbeek, D. Klinkenberg, T. D. Hollingsworth, How will country-based mitigation measures influence the course of the covid-19 epidemic? Lancet 395, 931–934 (2020). 2.  World Health Organization (WHO), Coronavirus disease (covid-19): Weekly epidemiological update (2020); https://who.int/emergencies/diseases/novel-coronavirus-2019/ situation-reports[accessed 15 October 2020]. 3.  E. Dong, H. Du, L. Gardner, An interactive web-based dashboard to track covid-19 in real time. Lancet Infect. Dis. 20, 533–534 (2020). 4.  C. Huang, Y. Wang, X. Li, L. Ren, J. Zhao, Y. Hu, L. Zhang, G. Fan, J. Xu, X. Gu, Z. Cheng, T. Yu, J. Xia, Y. Wei, W. Wu, X. Xie, W. Yin, H. Li, M. Liu, Y. Xiao, H. Gao, L. Guo, J. Xie, G. Wang, R. Jiang, Z. Gao, Q. Jin, J. Wang, B. Cao, Clinical features of patients infected with 2019 novel coronavirus in Wuhan, China. Lancet 395, 497–506 (2020). 5.  N. Chen, M. Zhou, X. Dong, J. Qu, F. Gong, Y. Han, Y. Qiu, J. Wang, Y. Liu, Y. Wei, J. Xia, T. Yu, X. Zhang, L. Zhang, Epidemiological and clinical characteristics of 99 cases of 2019 novel coronavirus pneumonia in Wuhan, China: A descriptive study. Lancet 395, 507–513 (2020). 6.  W. J. Wiersinga, A. Rhodes, A. C. Cheng, S. J. Peacock, H. C. Prescott, Pathophysiology, transmission, diagnosis, and treatment of coronavirus disease 2019 (COVID-19): A review. JAMA 324, 782–793 (2020). 7.  L. Wang, Y. Wang, D. Ye, Q. Liu, Review of the 2019 novel coronavirus (SARS-CoV-2) based on current evidence. Int. J. Antimicrob. Agents 55, 105948 (2020). 8.  E. Estrada, COVID-19 and SARS-CoV-2. Modeling the present, looking at the future. Phys. Rep. 869, 1–51 (2020). 9.  M. Chinazzi, J. T. Davis, M. Ajelli, C. Gioannini, M. Litvinova, S. Merler, A. Pastore y Piontti, K. Mu, L. Rossi, K. Sun, C. Viboud, X. Xiong, H. Yu, M. E. Halloran, I. M. Longini Jr., A. Vespignani, The effect of travel restrictions on the spread of the 2019 novel coronavirus (COVID-19) outbreak. Science 368, 395–400 (2020). 10.  K. Leung, J. T. Wu, D. Liu, G. M. Leung, First-wave covid-19 transmissibility and severity in China outside hubei after control measures, and second-wave scenario planning: A modelling impact assessment. Lancet 395, 1382–1393 (2020). 11.  P. Castorina, A. Iorio, D. Lanteri, Data analysis on coronavirus spreading by macroscopic growth laws. Int. J. Mod. Phys. C 31, 2050103 (2020). 12.  D. Lanteri, D. Carcò, P. Castorina, M. Ceccarelli, B. Cacopardo, Containment effort reduction and regrowth patterns of the COVID-19 spreading. arXiv:2004.14701 (2020). 13.  D. Fanelli, F. Piazza, Analysis and forecast of covid-19 spreading in china, italy and france. Chaos Solitons Fractals 134, 109761 (2020). 14.  A. Arenas, W. Cota, J. Gomez-Gardeñes, S. Gómez, C. Granell, J. T. Matamalas, D. Soriano, B. Steinegger, A mathematical model for the spatiotemporal epidemic spreading of COVID19. medRxiv (2020). 15.  A. J. Kucharski, T. W. Russell, C. Diamond, Y. Liu, J. Edmunds, S. Funk, R. M. Eggo, F. Sun, M. Jit, J. D. Munday, N. Davies, A. Gimma, K. van Zandvoort, H. Gibbs, J. Hellewell, C. I. Jarvis, S. Clifford, B. J. Quilty, N. I. Bosse, S. Abbott, P. Klepac, S. Flasche, Early dynamics of transmission and control of COVID-19: A mathematical modelling study. Lancet Infect. Dis. 20, 553–558 (2020). 16.  G. Giordano, F. Blanchini, R. Bruno, P. Colaneri, A. Di Filippo, A. Di Matteo, M. Colaneri, Modelling the COVID-19 epidemic and implementation of population-wide interventions in Italy. Nat. Med. 26, 855–860 (2020). 17.  A. Aleta, D. Martín-Corral, A. Pastore y Piontti, M. Ajelli, M. Litvinova, M. Chinazzi, N. E. Dean, M. E. Halloran, I. M. Longini Jr., S. Merler, A. Pentland, A. Vespignani, E. Moro,\n\nY. Moreno, Modelling the impact of testing, contact tracing and household quarantine on second waves of COVID-19. Nat. Hum. Behav. 4, 964–971 (2020). 18.  F. Della Rossa, D. Salzano, A. Di Meglio, F. De Lellis, M. Coraggio, C. Calabrese, A. Guarino, R. Cardona-Rivera, P. De Lellis, D. Liuzza, F. L. Iudice, G. Russo, M. di Bernardo, A network model of italy shows that intermittent regional strategies can alleviate the COVID-19 epidemic. Nat. Commun. 11, 5106 (2020). 19.  D. Proverbio, F. Kemp, S. Magni, A. Husch, A. Aalto, L. Mombaerts, A. Skupin, J. Gonçalves, J. Ameijeiras-Alonso, C. Ley, Dynamical SPQEIR model assesses the effectiveness of non-pharmaceutical interventions against COVID-19 epidemic outbreaks. PLOS ONE 16, e0252019 (2021). 20.  T. Heinemann, A. Raue, Model calibration and uncertainty analysis in signaling networks. Curr. Opin. Biotechnol. 39, 143–149 (2016). 21.  A. F. Villaverde, A. Barreiro, A. Papachristodoulou, Structural identifiability of dynamic systems biology models. PLOS Comput. Biol. 12, e1005153 (2016). 22.  G. Chowell, Fitting dynamic models to epidemic outbreaks with quantified uncertainty: A primer for parameter uncertainty, identifiability, and forecasts. Infect. Dis. Model. 2, 379–398 (2017). 23.  T. Quaiser, M. Mönnigmann, Systematic identifiability testing for unambiguous mechanistic modeling–application to JAK-STAT, MAP kinase, and NF- B signaling pathway models. BMC Syst. Biol. 3, 50 (2009). 24.  W. C. Roda, M. B. Varughese, D. Han, M. Y. Li, Why is it difficult to accurately predict the COVID-19 epidemic? Infect. Dis. Model. 5, 271–281 (2020). 25.  M. C. Eisenberg, S. L. Robertson, J. H. Tien, Identifiability and estimation of multiple transmission pathways in cholera and waterborne disease. J. Theor. Biol. 324, 84–102 (2013). 26.  N. Tuncer, T. T. Le, Structural and practical identifiability analysis of outbreak models. Math. Biosci. 299, 1–18 (2018). 27.  A. Raue, C. Kreutz, T. Maiwald, J. Bachmann, M. Schilling, U. Klingmüller, J. Timmer, Structural and practical identifiability analysis of partially observed dynamical models by exploiting the profile likelihood. Bioinformatics 25, 1923–1929 (2009). 28.  N. Tuncer, M. Marctheva, B. LaBarre, S. Payoute, Structural and practical identifiability analysis of zika epidemiological models. Bull. Math. Biol. 80, 2209–2241 (2018). 29.  K. Roosa, G. Chowell, Assessing parameter identifiability in compartmental dynamic models using a computational approach: Application to infectious disease transmission models. Theor. Biol. Med. Model. 16, 1–15 (2019). 30.  R. A. Horn, C. R. Johnson, Matrix Analysis (Cambridge Univ. press, 2012). 31.  K. Mizumoto, K. Kagaya, A. Zarebski, G. Chowell, Estimating the asymptomatic proportion of coronavirus disease 2019 (COVID-19) cases on board the diamond princess cruise ship, Yokohama, Japan, 2020. Eurosurveillance 25, 2000180 (2020). 32.  Q. Bi, Y. Wu, S. Mei, C. Ye, X. Zou, Z. Zhang, X. Liu, L. Wei, S. A. Truelove, T. Zhang, W. Gao, C. Cheng, X. Tang, X. Wu, Y. Wu, B. Sun, S. Huang, Y. Sun, J. Zhang, T. Ma, J. Lessler, T. Feng, Epidemiology and transmission of covid-19 in shenzhen china: Analysis of 391 cases and 1,286 of their close contacts. medRxiv (2020). 33.  E. Lavezzo, E. Franchin, C. Ciavarella, G. Cuomo-Dannenburg, L. Barzon, C. Del Vecchio, L. Rossi, R. Manganelli, A. Loregian, N. Navarin, D. Abate, M. Sciro, S. Merigliano, E. Decanale, M. C. Vanuzzo, F. Saluzzo, F. Onelia, M. Pacenti, S. Parisi, G. Carretta, D. Donato, L. Flor, S. Cocchio, G. Masi, A. Sperduti, L. Cattarino, R. Salvador, K. A. M. Gaythorpe; Imperial College London COVID- Response Team, A. R. Brazzale, S. Toppo, M. Trevisan, V. Baldo, C. A. Donnelly, N. M. Ferguson, I. Dorigatti, A. Crisanti, Suppression of COVID-19 outbreak in the municipality of Vo, Italy. medRxiv (2020). 34.  C. Liu, X. Wu, R. Niu, X. Wu, R. Fan, A new SAIR model on complex networks for analysing the 2019 novel coronavirus (COVID-19). Nonlinear Dyn., 1–11 (2020). 35.  R. H. Chisholm, P. T. Campbell, Y. Wu, S. Y. Tong, J. McVernon, N. Geard, Implications of asymptomatic carriers for infectious disease transmission and control. R. Soc. Open Sci. 5, 172341 (2018). 36.  L. Pribylova, V. Hajnova, SEIAR model with asymptomatic cohort and consequences to efficiency of quarantine government measures in COVID-19 epidemic. arXiv:2004.02601 (2020). 37.  J. B. Aguilar, J. S. Faust, L. M. Westafer, J. B. Gutierrez, Investigating the impact of asymptomatic carriers on COVID-19 transmission. medRxiv (2020). 38.  M. Robinson, N. I. Stilianakis, A model for the emergence of drug resistance in the presence of asymptomatic infections. Math. Biosci. 243, 163–177 (2013). 39.  D. Balcan, H. Hu, B. Goncalves, P. Bajardi, C. Poletto, J. J. Ramasco, D. Paolotti, N. Perra, M. Tizzoni, W. Van den Broeck, V. Colizza, A. Vespignani, Seasonal transmission potential and activity peaks of the new influenza A(H1N1): A Monte Carlo likelihood analysis based on human mobility. BMC Med. 7, 45 (2009). 40.  D. Balcan, B. Gonçalves, H. Hu, J. J. Ramasco, V. Colizza, A. Vespignani, Modeling the spatial spread of infectious diseases: The global epidemic and mobility computational model. J. Comput. Sci. 1, 132–145 (2010). 41.  X. He, E. H. Lau, P. Wu, X. Deng, J. Wang, X. Hao, Y. C. Lau, J. Y. Wong, Y. Guan, X. Tan, X. Mo, Y. Chen, B. Liao, W. Chen, F. Hu, Q. Zhang, M. Zhong, Y. Wu, L. Zhao, F. Zhang,\n\n12 of 13\n\nB. J. Cowling, F. Li, G. M. Leung, Temporal dynamics in viral shedding and transmissibility of COVID-19. Nat. Med. 26, 672–675 (2020). 42.  H. T. Banks, M. Davidian, J. R. Samuels, K. L. Sutton, G. Chowell, M. Hyman, L. M. A. Bettencourt, C. Castillo-Chavez, Chapter 11: Mathematical and Statistical Estimation Approaches in Epidemiology, in Mathematical and Statistical Estimation Approaches in Epidemiology, G. Chowell, J. M. Hyman, L. M. A. Bettencourt, and C. Castillo-Chavez, Eds. (Springer Netherlands, 2009) pp. 249–302. 43.  T. Toni, D. Welch, N. Strelkowa, A. Ipsen, M. P. H. Stumpf, Approximate Bayesian computation scheme for parameter inference and model selection in dynamical systems. J. R. Soc. Interface 6, 187–202 (2009). 44.  H. Haario, M. Laine, A. Mira, E. Saksman, DRAM: Efficient adaptive MCMC. Stat. Comput. 16, 339–354 (2006). 45.  A. Vespignani, H. Tian, C. Dye, J. O. Lloyd-Smith, R. M. Eggo, M. Shrestha, S. V. Scarpino, B. Gutierrez, M. U. G. Kraemer, J. Wu, K. Leung, G. M. Leung, Modelling COVID-19. Nat. Rev. Phys. 2, 279–281 (2020). 46.  J. Lourenço, R. Paton, M. Ghafari, M. Kraemer, C. Thompson, P. Simmonds, P. Klenerman, S. Gupta, Fundamental principles of epidemic spread highlight the immediate need for large-scale serological surveys to assess the stage of the SARS-CoV-2 epidemic. MedRxiv 2020.03.24.20042291 (2020). 47.  Presidenza del Consiglio dei Ministri (Presidency of the Council of Ministers), Governmental containment policies; http://governo.it/it/coronavirus-misure-del- governo[accessed 6 July 2020]. 48.  Presidenza del Consiglio dei Ministri (Presidency of the Council of Ministers), Legislation issued in response to covid-19 epidemic; http://governo.it/it/coronavirus- normativa[accessed 6 July 2020]. 49.  Dipartimento della Protezione Civile (Civil protection department), Data on the national trend; https://github.com/pcm-dpc/COVID-19/tree/master/dati-andamento- nazionale[accessed 06 July 2020]. 50.  R. Verity, L. C. Okell, I. Dorigatti, P. Winskill, C. Whittaker, N. Imai, G. Cuomo-Dannenburg, H. Thompson, P. G. T. Walker, H. Fu, A. Dighe, J. T. Griffin, M. Baguelin, S. Bhatia, A. Boonyasiri, A. Cori, Z. Cucunubá, R. F. John, K. Gaythorpe, W. Green, A. Hamlet, W. Hinsley, D. Laydon, G. Nedjati-Gilani, S. Riley, S. van Elsland, E. Volz, H. Wang, Y. Wang, X. Xi, C. A. Donnelly, A. C. Ghani, N. M. Ferguson, Estimates of the severity of coronavirus disease 2019: A model-based analysis. Lancet Infect. Dis. 20, 669–677 (2020). 51.  E. W. Griffith, K. S. P. Kumar, On the observability of nonlinear systems: I. J. Math. Anal. Appl. 35, 135–147 (1971).\n\n52.  W. S. Gray, J. P. Mesko, Observability functions for linear and nonlinear systems. Syst. Control Lett. 38, 99–113 (1999). 53.  A. C. Hindmarsh, Odepack, a systematized collection of ODE solvers. Sci. Comput. 1, 55–64 (1983). 54.  L. Petzold, Automatic selection of methods for solving stiff and nonstiff systems of ordinary differential equations. SIAM J. Sci. Stat. Comput. 4, 136–148 (1983). 55.  F. Kemp, D. Proverbio, A. Aalto, L. Mombaerts, A. Fouquier d\'Hérouël, A. Husch, C. Ley, J. Gonçalves, A. Skupin, S. Magni, Modelling COVID-19 dynamics and potential for herd immunity by vaccination in Austria, Luxembourg and Sweden. J. Theor. Biol. 530, 110874 (2021). 56.  G. E. P. Box, G. C. Tiao, Bayesian Inference in Statistical Analysis, vol. 40 (John Wiley & Sons, 2011).\nAcknowledgments: We would like to thank V. Simoncini for pointing out the relation between the sensitivity  and the generalized eigenvalue and D. Proverbio for the useful discussion on the Bayesian approach for parameter estimation. V.L. acknowledges support from the Leverhulme Trust Research Fellowship 278 "CREATE: The network components of creativity and success." V.L. and G.R. acknowledge support from University of Catania project "Piano della Ricerca 2020/2022, Linea d\'intervento 2, MOSCOVID." G.R. acknowledges support from Italian Ministry of Instruction, University and Research (MIUR) through PRIN project 2017, no. 2017KKJP4X. G.R. is a member of the "Istituto Nazionale di Alta Matematica Francesco Severi (INdAM) and "Gruppo Nazionale per il Calcolo Scientifico (GNCS). Author contributions: L.G., M.F., V.L., and G.R. conceived the research and developed the theory. L.G. carried out the numerical analysis. All authors wrote the manuscript. Competing interests: The authors declare that they have no competing interests. Data and materials availability: Epidemiological data displayed in Fig. 6 are publicly available data at the Italian Civil Protection repository (https://github.com/pcm-dpc/COVID-19/tree/master/dati-andamento- nazionale). Information about governmental containment policies in Italy are available at the Presidency of the Council of Ministers website (http://governo.it/it/coronavirus-misure-del- governo and http://governo.it/it/coronavirus-normativa). All the codes to perform the analyses discussed in the study and to produce Figs. 4 and 6 are made publicy available in the Zenodo repository (DOI: https://doi.org/10.5281/zenodo.5639320). All remaining data needed to evaluate the conclusions in the paper are present in the paper and/or the Supplementary Materials.\nSubmitted 11 January 2021 Accepted 24 November 2021 Published 19 January 2022 10.1126/sciadv.abg5234\n\n13 of 13\n\nLack of practical identifiability may hamper reliable predictions in COVID-19 epidemic models Luca Gallo, Mattia Frasca, Vito Latora, and Giovanni Russo\nSci. Adv., 8 (3), eabg5234. DOI: 10.1126/sciadv.abg5234\nView the article online https://www.science.org/doi/10.1126/sciadv.abg5234 Permissions https://www.science.org/help/reprints-and-permissions\nUse of this article is subject to the Terms of service\n\nScience Advances (ISSN ) is published by the American Association for the Advancement of Science. 1200 New York Avenue NW, Washington, DC 20005. The title Science Advances is a registered trademark of AAAS. Copyright © 2022 The Authors, some rights reserved; exclusive licensee American Association for the Advancement of Science. No claim to original U.S. Government Works. Distributed under a Creative Commons Attribution License 4.0 (CC BY).',
				grounding: null,
				assets: [
					{
						file_name: '1c43c93b-caf2-4287-a117-403622eaf689.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '2',
							bounding_box: [336, 670, 489, 738],
							detect_score: -4.1081390381,
							content: null,
							postprocess_score: 0.9940838218,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/1c43c93b-caf2-4287-a117-403622eaf689.png'
						}
					},
					{
						file_name: '73a0692d-2d91-447c-b4e1-2fed86995311.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '2',
							bounding_box: [251, 1319, 741, 1352],
							detect_score: -4.3372335434,
							content: null,
							postprocess_score: 0.993761003,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/73a0692d-2d91-447c-b4e1-2fed86995311.png'
						}
					},
					{
						file_name: '3ca57fd9-ad0b-4912-93f1-bcc499715328.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '2',
							bounding_box: [990, 1259, 1199, 1332],
							detect_score: -2.4051089287,
							content: null,
							postprocess_score: 0.9999140501,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/3ca57fd9-ad0b-4912-93f1-bcc499715328.png'
						}
					},
					{
						file_name: 'be3fa6fc-28d7-40c7-8fc0-4f22bc26797a.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '5',
							bounding_box: [1432, 707, 1456, 820],
							detect_score: -4.3379187584,
							content: null,
							postprocess_score: 0.5828691721,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/be3fa6fc-28d7-40c7-8fc0-4f22bc26797a.png'
						}
					},
					{
						file_name: '274447a5-ccc8-4674-a69a-6781904661e1.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '5',
							bounding_box: [1428, 861, 1456, 1067],
							detect_score: -4.3379187584,
							content: null,
							postprocess_score: 0.5828691721,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/274447a5-ccc8-4674-a69a-6781904661e1.png'
						}
					},
					{
						file_name: '2866515e-4786-4f33-8914-7cce286ccc7e.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '8',
							bounding_box: [913, 369, 1274, 435],
							detect_score: 2.6263093948,
							content: null,
							postprocess_score: 0.9999818802,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/2866515e-4786-4f33-8914-7cce286ccc7e.png'
						}
					},
					{
						file_name: '650f6fcb-498b-4395-875d-a4b9d2920a03.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '8',
							bounding_box: [961, 565, 1225, 598],
							detect_score: -0.6075288057,
							content: null,
							postprocess_score: 0.9982327223,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/650f6fcb-498b-4395-875d-a4b9d2920a03.png'
						}
					},
					{
						file_name: '7b8be0f6-5f87-4859-a106-e876f476ceb4.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '8',
							bounding_box: [991, 1381, 1198, 1444],
							detect_score: 1.3249536753,
							content: null,
							postprocess_score: 0.9998369217,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/7b8be0f6-5f87-4859-a106-e876f476ceb4.png'
						}
					},
					{
						file_name: '3ea0872a-a75c-482c-95b0-cc6ef1515f8b.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '8',
							bounding_box: [846, 1575, 1341, 1642],
							detect_score: -3.8546807766,
							content: null,
							postprocess_score: 0.9539873004,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/3ea0872a-a75c-482c-95b0-cc6ef1515f8b.png'
						}
					},
					{
						file_name: '77103b04-d8bc-4d48-98a1-1b8ed61a6d13.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [288, 165, 741, 198],
							detect_score: 1.7205687761,
							content: null,
							postprocess_score: 0.9992069602,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/77103b04-d8bc-4d48-98a1-1b8ed61a6d13.png'
						}
					},
					{
						file_name: '37430433-1c38-4b86-8a92-748389a0b657.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [356, 930, 475, 966],
							detect_score: -5.6010570526,
							content: null,
							postprocess_score: 0.9994494319,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/37430433-1c38-4b86-8a92-748389a0b657.png'
						}
					},
					{
						file_name: '8852aa0c-8073-4631-96b8-73592d9c8bc0.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [327, 1359, 502, 1400],
							detect_score: -1.6192967892,
							content: null,
							postprocess_score: 0.9999527931,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/8852aa0c-8073-4631-96b8-73592d9c8bc0.png'
						}
					},
					{
						file_name: '82fdefb0-c705-402c-9d5b-5ab7bd53cd15.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [335, 1717, 497, 1793],
							detect_score: -2.3205866814,
							content: null,
							postprocess_score: 0.9999281168,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/82fdefb0-c705-402c-9d5b-5ab7bd53cd15.png'
						}
					},
					{
						file_name: 'a6f959bd-88a4-429d-9c1a-00ddb2d1c82b.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [1021, 596, 1166, 626],
							detect_score: -3.6076159477,
							content: null,
							postprocess_score: 0.9901427031,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/a6f959bd-88a4-429d-9c1a-00ddb2d1c82b.png'
						}
					},
					{
						file_name: '4c30d803-2b0f-43aa-b7b4-dc0ab7d8c7ee.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '9',
							bounding_box: [1060, 1397, 1127, 1442],
							detect_score: -4.5055198669,
							content: null,
							postprocess_score: 0.9997122884,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/4c30d803-2b0f-43aa-b7b4-dc0ab7d8c7ee.png'
						}
					},
					{
						file_name: 'b1cc27cd-7f17-414d-8356-7380275e658d.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '10',
							bounding_box: [141, 303, 654, 437],
							detect_score: -1.4274843931,
							content: null,
							postprocess_score: 0.9908812046,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/b1cc27cd-7f17-414d-8356-7380275e658d.png'
						}
					},
					{
						file_name: '3fb610ca-4f48-45ea-bad3-8065ca3b82cd.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '11',
							bounding_box: [262, 144, 579, 218],
							detect_score: -1.3325997591,
							content: null,
							postprocess_score: 0.9999341965,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/3fb610ca-4f48-45ea-bad3-8065ca3b82cd.png'
						}
					},
					{
						file_name: '4c4ebda3-a36d-4df7-810d-68534fed974c.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '13',
							bounding_box: [1432, 707, 1456, 820],
							detect_score: -5.493847847,
							content: null,
							postprocess_score: 0.7526928186,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/4c4ebda3-a36d-4df7-810d-68534fed974c.png'
						}
					},
					{
						file_name: 'c925c604-9978-4ab7-a0c1-3bf483f34213.png',
						asset_type: 'equation',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '13',
							bounding_box: [1428, 861, 1456, 1067],
							detect_score: -5.493847847,
							content: null,
							postprocess_score: 0.7526928186,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/c925c604-9978-4ab7-a0c1-3bf483f34213.png'
						}
					},
					{
						file_name: '3dc4e8a9-e111-4397-8768-6abe2b78fe3d.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '3',
							bounding_box: [774, 153, 1412, 514],
							detect_score: 0.2607835531,
							content: '',
							postprocess_score: 0.9998978376,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/3dc4e8a9-e111-4397-8768-6abe2b78fe3d.png'
						}
					},
					{
						file_name: '9d7124c4-0838-4c9d-976f-60789fa6fe95.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '5',
							bounding_box: [207, 813, 741, 1660],
							detect_score: -4.2762527466,
							content: '',
							postprocess_score: 0.997459352,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/9d7124c4-0838-4c9d-976f-60789fa6fe95.png'
						}
					},
					{
						file_name: 'd068f89d-14af-445b-a544-c40aaf0d60cf.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '5',
							bounding_box: [767, 813, 1302, 1660],
							detect_score: -3.1280417442,
							content: '',
							postprocess_score: 0.9994435906,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/d068f89d-14af-445b-a544-c40aaf0d60cf.png'
						}
					},
					{
						file_name: '2e436229-4e44-4f95-a491-60ea3fb23da9.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '6',
							bounding_box: [298, 153, 1210, 624],
							detect_score: -0.0541875176,
							content: '',
							postprocess_score: 0.9999186993,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/2e436229-4e44-4f95-a491-60ea3fb23da9.png'
						}
					},
					{
						file_name: '8695e4c7-bbf1-40a6-a921-38317a9d413a.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '7',
							bounding_box: [207, 153, 733, 358],
							detect_score: -3.6461851597,
							content: '',
							postprocess_score: 0.9988308549,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/8695e4c7-bbf1-40a6-a921-38317a9d413a.png'
						}
					},
					{
						file_name: '7b77c55a-7a83-46c5-adbc-0af3b75ab247.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '7',
							bounding_box: [217, 374, 733, 579],
							detect_score: -4.6077470779,
							content: '',
							postprocess_score: 0.9993357062,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/7b77c55a-7a83-46c5-adbc-0af3b75ab247.png'
						}
					},
					{
						file_name: '1b77e2d1-976a-4d61-91f1-131e07e5cc57.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '7',
							bounding_box: [785, 153, 1302, 358],
							detect_score: -2.8508985043,
							content: '',
							postprocess_score: 0.998703599,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/1b77e2d1-976a-4d61-91f1-131e07e5cc57.png'
						}
					},
					{
						file_name: 'e5979e78-f1b2-4767-b39b-a9da01a995d7.png',
						asset_type: 'figure',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '7',
							bounding_box: [785, 374, 1302, 579],
							detect_score: -3.6906137466,
							content: '',
							postprocess_score: 0.9977636337,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/e5979e78-f1b2-4767-b39b-a9da01a995d7.png'
						}
					},
					{
						file_name: '8c166f76-962f-4982-8c60-682a93eb47ef.png',
						asset_type: 'table',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '5',
							bounding_box: [91, 541, 739, 789],
							detect_score: -6.910340786,
							content:
								'Table 1. Values of , , and  for the four configurations of the SIAR model shown in Fig. 3. Case A Case B Case C Case D  0.0096 0.15 0.013 0.091  0.012 0.16 0.36 1.4  34 5.2 29 15',
							postprocess_score: 0.9030431509,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/8c166f76-962f-4982-8c60-682a93eb47ef.png'
						}
					},
					{
						file_name: '6115fdf2-c856-41bf-874d-7572969d3c15.png',
						asset_type: 'table',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '10',
							bounding_box: [769, 150, 1417, 323],
							detect_score: -0.5080314279,
							content:
								'Table 2. Values of the model parameters used for the case study in Fig. 2. 0 a0 r0 I A IR AR AI 0.05 0.1 0 0.6 0.3 0.1 0.2 0.03',
							postprocess_score: 0.9989734888,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/6115fdf2-c856-41bf-874d-7572969d3c15.png'
						}
					},
					{
						file_name: '812b17c8-bfb3-4d4c-a5b6-7c206cc8c2f2.png',
						asset_type: 'table',
						metadata: {
							pdf_name: 'paper.pdf',
							page_num: '10',
							bounding_box: [769, 356, 1417, 630],
							detect_score: -4.5122990608,
							content:
								'Table 3. Values of the model parameters used for the case study in Fig. 3.  0 a0 r0 I A IR AR AI Case A 0.1 0.2 0.05 0.3 0.4 0.26 0.1 0.2 0.03 Case B 0.05 0.1 0 0.6 0.3 0.51 0.1 0.2 0.03 Case C 0.1 0.2 0.05 0.6 0.8 0.77 0.1 0.2 0.1 Case D 0.1 0.2 0.05 0.3 0.4 0.53 0.1 0.2 0.03',
							postprocess_score: 0.9844682813,
							img_pth:
								'http://xdd.wisc.edu/cosmos_service/process/5500f517-e76e-46bf-b55d-8e6f03bbe87e/result/images/812b17c8-bfb3-4d4c-a5b6-7c206cc8c2f2.png'
						}
					}
				]
			}
		];
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get projects publication assets for a given project per id
 * @param projectId projet id to get assets for
 * @return ExternalPublication[] the documents assets for the project
 */
async function getPublicationAssets(projectId: string): Promise<ExternalPublication[]> {
	try {
		const url = `/projects/${projectId}/assets?types=${AssetType.Publications}`;
		const response = await API.get(url);
		const { status, data } = response;
		if (status === 200) {
			return data?.[AssetType.Publications] ?? ([] as ExternalPublication[]);
		}
	} catch (error) {
		logger.error(error);
	}
	return [] as ExternalPublication[];
}

/**
 * Add project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'documents'
 * @assetId string - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return any|null - some result if success, or null if none returned by API
 */
async function addAsset(projectId: string, assetsType: string, assetId: string) {
	// FIXME: handle cases where assets is already added to the project
	const url = `/projects/${projectId}/assets/${assetsType}/${assetId}`;
	const response = await API.post(url);

	EventService.create(
		EventType.AddResourcesToProject,
		projectId,
		JSON.stringify({
			assetsType,
			assetId
		})
	);
	return response?.data ?? null;
}

/**
 * Delete a project asset
 * @projectId IProject["id"] - represents the project id wherein the asset will be added
 * @assetType AssetType - represents the type of asset to be added, e.g., 'documents'
 * @assetId string | number - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return boolean
 */
async function deleteAsset(
	projectId: IProject['id'],
	assetType: AssetType,
	assetId: string | number
): Promise<boolean> {
	try {
		const url = `/projects/${projectId}/assets/${assetType}/${assetId}`;
		const { status } = await API.delete(url);
		return status >= 200 && status < 300;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

/**
 * Get a project per id
 * @param projectId - string
 * @param containingAssetsInformation - boolean - Add the assets information during the same call
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function get(
	projectId: string,
	containingAssetsInformation: boolean = false
): Promise<IProject | null> {
	try {
		const { status, data } = await API.get(`/projects/${projectId}`);
		if (status !== 200) return null;
		const project = data as IProject;

		if (project && containingAssetsInformation) {
			const assets = await getAssets(projectId);
			if (assets) {
				project.assets = assets;
			}
		}

		return project ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get the icon associated with an Asset
 */
const icons = new Map<string | AssetType, string | Component>([
	[AssetType.Publications, 'file'],
	[AssetType.Models, 'share-2'],
	[AssetType.Datasets, DatasetIcon],
	[AssetType.Simulations, 'settings'],
	[AssetType.Code, 'code'],
	[AssetType.Workflows, 'git-merge'],
	['overview', 'layout']
]);

function getAssetIcon(type: AssetType | string | null): string | Component {
	if (type && icons.has(type)) {
		return icons.get(type) ?? 'circle';
	}
	return 'circle';
}

export {
	create,
	update,
	get,
	remove,
	getAll,
	addAsset,
	deleteAsset,
	getAssets,
	getAssetIcon,
	getPublicationAssets
};
