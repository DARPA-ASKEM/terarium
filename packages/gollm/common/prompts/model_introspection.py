MODEL_INTROSPECTION_PROMPT = """
You are an expert in global trades, supply chains, and geopolitics.

Your job is to assess which parts of the model should be tweaked to address the question/concern below.

The model represents mineral trade/export across time amongst different countries: CD, CN, RW, SA, US, ZM

The model is specified in LaTeX as a ODE-system.

{ode}

\\begin{{align}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}} ={{}} g_{{CD}} \\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}} ={{}} g_{{CN}} \\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}} ={{}} g_{{RW}} \\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}} ={{}} g_{{SA}} \\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}} ={{}} g_{{US}} \\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}} ={{}} g_{{ZM}} \\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CD CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{CD CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CD CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{CD CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CD CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{CD CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CD CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{CD CoUnwrought}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CN CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{CN CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CN CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{CN CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CN CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{CN CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{CN CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{CN CoUnwrought}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{RW CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{RW CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{RW CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{RW CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{RW CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{RW CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{RW CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{RW CoUnwrought}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{SA CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{SA CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{SA CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{SA CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{SA CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{SA CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{SA CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{SA CoUnwrought}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{US CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{US CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{US CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{US CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{US CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{US CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{US CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{US CoUnwrought}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{ZM CoArticles}}{{\\left(t \\right)}} ={{}} - SP_{{ZM CoArticles}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{ZM CoOre}}{{\\left(t \\right)}} ={{}} - SP_{{ZM CoOre}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{ZM CoOxides}}{{\\left(t \\right)}} ={{}} - SP_{{ZM CoOxides}}
  \\frac{{d}}{{d t}} \\operatorname{{OS}}_{{ZM CoUnwrought}}{{\\left(t \\right)}} ={{}} - SP_{{ZM CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{CD CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{CD CoArticles}}
  \\frac{{d}}{{d t}} R_{{CD CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{CD CoOre}}
  \\frac{{d}}{{d t}} R_{{CD CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{CD CoOxides}}
  \\frac{{d}}{{d t}} R_{{CD CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{CD CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{CN CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{CN CoArticles}}
  \\frac{{d}}{{d t}} R_{{CN CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{CN CoOre}}
  \\frac{{d}}{{d t}} R_{{CN CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{CN CoOxides}}
  \\frac{{d}}{{d t}} R_{{CN CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{CN CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{RW CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{RW CoArticles}}
  \\frac{{d}}{{d t}} R_{{RW CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{RW CoOre}}
  \\frac{{d}}{{d t}} R_{{RW CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{RW CoOxides}}
  \\frac{{d}}{{d t}} R_{{RW CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{RW CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{SA CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{SA CoArticles}}
  \\frac{{d}}{{d t}} R_{{SA CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{SA CoOre}}
  \\frac{{d}}{{d t}} R_{{SA CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{SA CoOxides}}
  \\frac{{d}}{{d t}} R_{{SA CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{SA CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{US CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{US CoArticles}}
  \\frac{{d}}{{d t}} R_{{US CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{US CoOre}}
  \\frac{{d}}{{d t}} R_{{US CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{US CoOxides}}
  \\frac{{d}}{{d t}} R_{{US CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{US CoUnwrought}}
  \\frac{{d}}{{d t}} R_{{ZM CoArticles}}{{\\left(t \\right)}} ={{}} - PP_{{ZM CoArticles}}
  \\frac{{d}}{{d t}} R_{{ZM CoOre}}{{\\left(t \\right)}} ={{}} - PP_{{ZM CoOre}}
  \\frac{{d}}{{d t}} R_{{ZM CoOxides}}{{\\left(t \\right)}} ={{}} - PP_{{ZM CoOxides}}
  \\frac{{d}}{{d t}} R_{{ZM CoUnwrought}}{{\\left(t \\right)}} ={{}} - PP_{{ZM CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{CD CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{CD CoArticles}} - IE_{{CD CN CoArticles}} - IE_{{CD RW CoArticles}} - IE_{{CD SA CoArticles}} - IE_{{CD US CoArticles}} - IE_{{CD ZM CoArticles}} + IE_{{CN CD CoArticles}} + IE_{{RW CD CoArticles}} + IE_{{SA CD CoArticles}} + IE_{{US CD CoArticles}} + IE_{{ZM CD CoArticles}} + PP_{{CD CoArticles}} + SP_{{CD CoArticles}}
  \\frac{{d}}{{d t}} S_{{CD CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{CD CoOre}} - IE_{{CD CN CoOre}} - IE_{{CD RW CoOre}} - IE_{{CD SA CoOre}} - IE_{{CD US CoOre}} - IE_{{CD ZM CoOre}} + IE_{{CN CD CoOre}} + IE_{{RW CD CoOre}} + IE_{{SA CD CoOre}} + IE_{{US CD CoOre}} + IE_{{ZM CD CoOre}} + PP_{{CD CoOre}} + SP_{{CD CoOre}}
  \\frac{{d}}{{d t}} S_{{CD CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{CD CoOxides}} - IE_{{CD CN CoOxides}} - IE_{{CD RW CoOxides}} - IE_{{CD SA CoOxides}} - IE_{{CD US CoOxides}} - IE_{{CD ZM CoOxides}} + IE_{{CN CD CoOxides}} + IE_{{RW CD CoOxides}} + IE_{{SA CD CoOxides}} + IE_{{US CD CoOxides}} + IE_{{ZM CD CoOxides}} + PP_{{CD CoOxides}} + SP_{{CD CoOxides}}
  \\frac{{d}}{{d t}} S_{{CD CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{CD CoUnwrought}} - IE_{{CD CN CoUnwrought}} - IE_{{CD RW CoUnwrought}} - IE_{{CD SA CoUnwrought}} - IE_{{CD US CoUnwrought}} - IE_{{CD ZM CoUnwrought}} + IE_{{CN CD CoUnwrought}} + IE_{{RW CD CoUnwrought}} + IE_{{SA CD CoUnwrought}} + IE_{{US CD CoUnwrought}} + IE_{{ZM CD CoUnwrought}} + PP_{{CD CoUnwrought}} + SP_{{CD CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{CN CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{CN CoArticles}} + IE_{{CD CN CoArticles}} - IE_{{CN CD CoArticles}} - IE_{{CN RW CoArticles}} - IE_{{CN SA CoArticles}} - IE_{{CN US CoArticles}} - IE_{{CN ZM CoArticles}} + IE_{{RW CN CoArticles}} + IE_{{SA CN CoArticles}} + IE_{{US CN CoArticles}} + IE_{{ZM CN CoArticles}} + PP_{{CN CoArticles}} + SP_{{CN CoArticles}}
  \\frac{{d}}{{d t}} S_{{CN CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{CN CoOre}} + IE_{{CD CN CoOre}} - IE_{{CN CD CoOre}} - IE_{{CN RW CoOre}} - IE_{{CN SA CoOre}} - IE_{{CN US CoOre}} - IE_{{CN ZM CoOre}} + IE_{{RW CN CoOre}} + IE_{{SA CN CoOre}} + IE_{{US CN CoOre}} + IE_{{ZM CN CoOre}} + PP_{{CN CoOre}} + SP_{{CN CoOre}}
  \\frac{{d}}{{d t}} S_{{CN CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{CN CoOxides}} + IE_{{CD CN CoOxides}} - IE_{{CN CD CoOxides}} - IE_{{CN RW CoOxides}} - IE_{{CN SA CoOxides}} - IE_{{CN US CoOxides}} - IE_{{CN ZM CoOxides}} + IE_{{RW CN CoOxides}} + IE_{{SA CN CoOxides}} + IE_{{US CN CoOxides}} + IE_{{ZM CN CoOxides}} + PP_{{CN CoOxides}} + SP_{{CN CoOxides}}
  \\frac{{d}}{{d t}} S_{{CN CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{CN CoUnwrought}} + IE_{{CD CN CoUnwrought}} - IE_{{CN CD CoUnwrought}} - IE_{{CN RW CoUnwrought}} - IE_{{CN SA CoUnwrought}} - IE_{{CN US CoUnwrought}} - IE_{{CN ZM CoUnwrought}} + IE_{{RW CN CoUnwrought}} + IE_{{SA CN CoUnwrought}} + IE_{{US CN CoUnwrought}} + IE_{{ZM CN CoUnwrought}} + PP_{{CN CoUnwrought}} + SP_{{CN CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{RW CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{RW CoArticles}} + IE_{{CD RW CoArticles}} + IE_{{CN RW CoArticles}} - IE_{{RW CD CoArticles}} - IE_{{RW CN CoArticles}} - IE_{{RW SA CoArticles}} - IE_{{RW US CoArticles}} - IE_{{RW ZM CoArticles}} + IE_{{SA RW CoArticles}} + IE_{{US RW CoArticles}} + IE_{{ZM RW CoArticles}} + PP_{{RW CoArticles}} + SP_{{RW CoArticles}}
  \\frac{{d}}{{d t}} S_{{RW CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{RW CoOre}} + IE_{{CD RW CoOre}} + IE_{{CN RW CoOre}} - IE_{{RW CD CoOre}} - IE_{{RW CN CoOre}} - IE_{{RW SA CoOre}} - IE_{{RW US CoOre}} - IE_{{RW ZM CoOre}} + IE_{{SA RW CoOre}} + IE_{{US RW CoOre}} + IE_{{ZM RW CoOre}} + PP_{{RW CoOre}} + SP_{{RW CoOre}}
  \\frac{{d}}{{d t}} S_{{RW CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{RW CoOxides}} + IE_{{CD RW CoOxides}} + IE_{{CN RW CoOxides}} - IE_{{RW CD CoOxides}} - IE_{{RW CN CoOxides}} - IE_{{RW SA CoOxides}} - IE_{{RW US CoOxides}} - IE_{{RW ZM CoOxides}} + IE_{{SA RW CoOxides}} + IE_{{US RW CoOxides}} + IE_{{ZM RW CoOxides}} + PP_{{RW CoOxides}} + SP_{{RW CoOxides}}
  \\frac{{d}}{{d t}} S_{{RW CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{RW CoUnwrought}} + IE_{{CD RW CoUnwrought}} + IE_{{CN RW CoUnwrought}} - IE_{{RW CD CoUnwrought}} - IE_{{RW CN CoUnwrought}} - IE_{{RW SA CoUnwrought}} - IE_{{RW US CoUnwrought}} - IE_{{RW ZM CoUnwrought}} + IE_{{SA RW CoUnwrought}} + IE_{{US RW CoUnwrought}} + IE_{{ZM RW CoUnwrought}} + PP_{{RW CoUnwrought}} + SP_{{RW CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{SA CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{SA CoArticles}} + IE_{{CD SA CoArticles}} + IE_{{CN SA CoArticles}} + IE_{{RW SA CoArticles}} - IE_{{SA CD CoArticles}} - IE_{{SA CN CoArticles}} - IE_{{SA RW CoArticles}} - IE_{{SA US CoArticles}} - IE_{{SA ZM CoArticles}} + IE_{{US SA CoArticles}} + IE_{{ZM SA CoArticles}} + PP_{{SA CoArticles}} + SP_{{SA CoArticles}}
  \\frac{{d}}{{d t}} S_{{SA CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{SA CoOre}} + IE_{{CD SA CoOre}} + IE_{{CN SA CoOre}} + IE_{{RW SA CoOre}} - IE_{{SA CD CoOre}} - IE_{{SA CN CoOre}} - IE_{{SA RW CoOre}} - IE_{{SA US CoOre}} - IE_{{SA ZM CoOre}} + IE_{{US SA CoOre}} + IE_{{ZM SA CoOre}} + PP_{{SA CoOre}} + SP_{{SA CoOre}}
  \\frac{{d}}{{d t}} S_{{SA CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{SA CoOxides}} + IE_{{CD SA CoOxides}} + IE_{{CN SA CoOxides}} + IE_{{RW SA CoOxides}} - IE_{{SA CD CoOxides}} - IE_{{SA CN CoOxides}} - IE_{{SA RW CoOxides}} - IE_{{SA US CoOxides}} - IE_{{SA ZM CoOxides}} + IE_{{US SA CoOxides}} + IE_{{ZM SA CoOxides}} + PP_{{SA CoOxides}} + SP_{{SA CoOxides}}
  \\frac{{d}}{{d t}} S_{{SA CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{SA CoUnwrought}} + IE_{{CD SA CoUnwrought}} + IE_{{CN SA CoUnwrought}} + IE_{{RW SA CoUnwrought}} - IE_{{SA CD CoUnwrought}} - IE_{{SA CN CoUnwrought}} - IE_{{SA RW CoUnwrought}} - IE_{{SA US CoUnwrought}} - IE_{{SA ZM CoUnwrought}} + IE_{{US SA CoUnwrought}} + IE_{{ZM SA CoUnwrought}} + PP_{{SA CoUnwrought}} + SP_{{SA CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{US CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{US CoArticles}} + IE_{{CD US CoArticles}} + IE_{{CN US CoArticles}} + IE_{{RW US CoArticles}} + IE_{{SA US CoArticles}} - IE_{{US CD CoArticles}} - IE_{{US CN CoArticles}} - IE_{{US RW CoArticles}} - IE_{{US SA CoArticles}} - IE_{{US ZM CoArticles}} + IE_{{ZM US CoArticles}} + PP_{{US CoArticles}} + SP_{{US CoArticles}}
  \\frac{{d}}{{d t}} S_{{US CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{US CoOre}} + IE_{{CD US CoOre}} + IE_{{CN US CoOre}} + IE_{{RW US CoOre}} + IE_{{SA US CoOre}} - IE_{{US CD CoOre}} - IE_{{US CN CoOre}} - IE_{{US RW CoOre}} - IE_{{US SA CoOre}} - IE_{{US ZM CoOre}} + IE_{{ZM US CoOre}} + PP_{{US CoOre}} + SP_{{US CoOre}}
  \\frac{{d}}{{d t}} S_{{US CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{US CoOxides}} + IE_{{CD US CoOxides}} + IE_{{CN US CoOxides}} + IE_{{RW US CoOxides}} + IE_{{SA US CoOxides}} - IE_{{US CD CoOxides}} - IE_{{US CN CoOxides}} - IE_{{US RW CoOxides}} - IE_{{US SA CoOxides}} - IE_{{US ZM CoOxides}} + IE_{{ZM US CoOxides}} + PP_{{US CoOxides}} + SP_{{US CoOxides}}
  \\frac{{d}}{{d t}} S_{{US CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{US CoUnwrought}} + IE_{{CD US CoUnwrought}} + IE_{{CN US CoUnwrought}} + IE_{{RW US CoUnwrought}} + IE_{{SA US CoUnwrought}} - IE_{{US CD CoUnwrought}} - IE_{{US CN CoUnwrought}} - IE_{{US RW CoUnwrought}} - IE_{{US SA CoUnwrought}} - IE_{{US ZM CoUnwrought}} + IE_{{ZM US CoUnwrought}} + PP_{{US CoUnwrought}} + SP_{{US CoUnwrought}}
  \\frac{{d}}{{d t}} S_{{ZM CoArticles}}{{\\left(t \\right)}} ={{}} DeltaS_{{ZM CoArticles}} + IE_{{CD ZM CoArticles}} + IE_{{CN ZM CoArticles}} + IE_{{RW ZM CoArticles}} + IE_{{SA ZM CoArticles}} + IE_{{US ZM CoArticles}} - IE_{{ZM CD CoArticles}} - IE_{{ZM CN CoArticles}} - IE_{{ZM RW CoArticles}} - IE_{{ZM SA CoArticles}} - IE_{{ZM US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{ZM CoArticles}}
  \\frac{{d}}{{d t}} S_{{ZM CoOre}}{{\\left(t \\right)}} ={{}} DeltaS_{{ZM CoOre}} + IE_{{CD ZM CoOre}} + IE_{{CN ZM CoOre}} + IE_{{RW ZM CoOre}} + IE_{{SA ZM CoOre}} + IE_{{US ZM CoOre}} - IE_{{ZM CD CoOre}} - IE_{{ZM CN CoOre}} - IE_{{ZM RW CoOre}} - IE_{{ZM SA CoOre}} - IE_{{ZM US CoOre}} + PP_{{ZM CoOre}} + SP_{{ZM CoOre}}
  \\frac{{d}}{{d t}} S_{{ZM CoOxides}}{{\\left(t \\right)}} ={{}} DeltaS_{{ZM CoOxides}} + IE_{{CD ZM CoOxides}} + IE_{{CN ZM CoOxides}} + IE_{{RW ZM CoOxides}} + IE_{{SA ZM CoOxides}} + IE_{{US ZM CoOxides}} - IE_{{ZM CD CoOxides}} - IE_{{ZM CN CoOxides}} - IE_{{ZM RW CoOxides}} - IE_{{ZM SA CoOxides}} - IE_{{ZM US CoOxides}} + PP_{{ZM CoOxides}} + SP_{{ZM CoOxides}}
  \\frac{{d}}{{d t}} S_{{ZM CoUnwrought}}{{\\left(t \\right)}} ={{}} DeltaS_{{ZM CoUnwrought}} + IE_{{CD ZM CoUnwrought}} + IE_{{CN ZM CoUnwrought}} + IE_{{RW ZM CoUnwrought}} + IE_{{SA ZM CoUnwrought}} + IE_{{US ZM CoUnwrought}} - IE_{{ZM CD CoUnwrought}} - IE_{{ZM CN CoUnwrought}} - IE_{{ZM RW CoUnwrought}} - IE_{{ZM SA CoUnwrought}} - IE_{{ZM US CoUnwrought}} + PP_{{ZM CoUnwrought}} + SP_{{ZM CoUnwrought}}

  \\operatorname{{ASI}}_{{CD}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{CD}}}}{{100}}
  \\operatorname{{ASI}}_{{CN}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{CN}}}}{{100}}
  \\operatorname{{ASI}}_{{RW}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{RW}}}}{{100}}
  \\operatorname{{ASI}}_{{SA}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{SA}}}}{{100}}
  \\operatorname{{ASI}}_{{US}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{US}}}}{{100}}
  \\operatorname{{ASI}}_{{ZM}}{{\\left(t \\right)}} ={{}} 1 - \\frac{{PPI_{{ZM}}}}{{100}}
  \\operatorname{{DP}}_{{CoArticles}}{{\\left(t \\right)}} ={{}} ASI_{{CN}} PS_{{CD CoArticles}}^{{2}} WSI_{{us CD}} + ASI_{{CN}} PS_{{CN CoArticles}}^{{2}} WSI_{{us CN}} + ASI_{{CN}} PS_{{RW CoArticles}}^{{2}} WSI_{{us RW}} + ASI_{{CN}} PS_{{SA CoArticles}}^{{2}} WSI_{{us SA}} + ASI_{{CN}} PS_{{US CoArticles}}^{{2}} WSI_{{us US}} + ASI_{{CN}} PS_{{ZM CoArticles}}^{{2}} WSI_{{us ZM}}
  \\operatorname{{DP}}_{{CoOre}}{{\\left(t \\right)}} ={{}} ASI_{{CN}} PS_{{CD CoOre}}^{{2}} WSI_{{us CD}} + ASI_{{CN}} PS_{{CN CoOre}}^{{2}} WSI_{{us CN}} + ASI_{{CN}} PS_{{RW CoOre}}^{{2}} WSI_{{us RW}} + ASI_{{CN}} PS_{{SA CoOre}}^{{2}} WSI_{{us SA}} + ASI_{{CN}} PS_{{US CoOre}}^{{2}} WSI_{{us US}} + ASI_{{CN}} PS_{{ZM CoOre}}^{{2}} WSI_{{us ZM}}
  \\operatorname{{DP}}_{{CoOxides}}{{\\left(t \\right)}} ={{}} ASI_{{CN}} PS_{{CD CoOxides}}^{{2}} WSI_{{us CD}} + ASI_{{CN}} PS_{{CN CoOxides}}^{{2}} WSI_{{us CN}} + ASI_{{CN}} PS_{{RW CoOxides}}^{{2}} WSI_{{us RW}} + ASI_{{CN}} PS_{{SA CoOxides}}^{{2}} WSI_{{us SA}} + ASI_{{CN}} PS_{{US CoOxides}}^{{2}} WSI_{{us US}} + ASI_{{CN}} PS_{{ZM CoOxides}}^{{2}} WSI_{{us ZM}}
  \\operatorname{{DP}}_{{CoUnwrought}}{{\\left(t \\right)}} ={{}} ASI_{{CN}} PS_{{CD CoUnwrought}}^{{2}} WSI_{{us CD}} + ASI_{{CN}} PS_{{CN CoUnwrought}}^{{2}} WSI_{{us CN}} + ASI_{{CN}} PS_{{RW CoUnwrought}}^{{2}} WSI_{{us RW}} + ASI_{{CN}} PS_{{SA CoUnwrought}}^{{2}} WSI_{{us SA}} + ASI_{{CN}} PS_{{US CoUnwrought}}^{{2}} WSI_{{us US}} + ASI_{{CN}} PS_{{ZM CoUnwrought}}^{{2}} WSI_{{us ZM}}
  \\operatorname{{EV}}_{{us CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{\\frac{{EXP_{{magneticalloys CoArticles}} VA_{{magneticalloys}}}}{{OP_{{magneticalloys}}}} + \\frac{{EXP_{{steels CoArticles}} VA_{{steels}}}}{{OP_{{steels}}}} + \\frac{{EXP_{{superalloys CoArticles}} VA_{{superalloys}}}}{{OP_{{superalloys}}}}}}{{GDP_{{us}}}}
  \\operatorname{{EV}}_{{us CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{\\frac{{EXP_{{magneticalloys CoOre}} VA_{{magneticalloys}}}}{{OP_{{magneticalloys}}}} + \\frac{{EXP_{{steels CoOre}} VA_{{steels}}}}{{OP_{{steels}}}} + \\frac{{EXP_{{superalloys CoOre}} VA_{{superalloys}}}}{{OP_{{superalloys}}}}}}{{GDP_{{us}}}}
  \\operatorname{{EV}}_{{us CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{\\frac{{EXP_{{magneticalloys CoOxides}} VA_{{magneticalloys}}}}{{OP_{{magneticalloys}}}} + \\frac{{EXP_{{steels CoOxides}} VA_{{steels}}}}{{OP_{{steels}}}} + \\frac{{EXP_{{superalloys CoOxides}} VA_{{superalloys}}}}{{OP_{{superalloys}}}}}}{{GDP_{{us}}}}
  \\operatorname{{EV}}_{{us CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{\\frac{{EXP_{{magneticalloys CoUnwrought}} VA_{{magneticalloys}}}}{{OP_{{magneticalloys}}}} + \\frac{{EXP_{{steels CoUnwrought}} VA_{{steels}}}}{{OP_{{steels}}}} + \\frac{{EXP_{{superalloys CoUnwrought}} VA_{{superalloys}}}}{{OP_{{superalloys}}}}}}{{GDP_{{us}}}}
  \\operatorname{{PS}}_{{CD CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{CD CoArticles}} + SP_{{CD CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{PS}}_{{CN CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{CN CoArticles}} + SP_{{CN CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{PS}}_{{RW CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{RW CoArticles}} + SP_{{RW CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{PS}}_{{SA CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{SA CoArticles}} + SP_{{SA CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{PS}}_{{US CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{US CoArticles}} + SP_{{US CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{PS}}_{{ZM CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{PP_{{ZM CoArticles}} + SP_{{ZM CoArticles}}}}{{PP_{{CD CoArticles}} + PP_{{CN CoArticles}} + PP_{{RW CoArticles}} + PP_{{SA CoArticles}} + PP_{{US CoArticles}} + PP_{{ZM CoArticles}} + SP_{{CD CoArticles}} + SP_{{CN CoArticles}} + SP_{{RW CoArticles}} + SP_{{SA CoArticles}} + SP_{{US CoArticles}} + SP_{{ZM CoArticles}}}}
  \\operatorname{{SR}}_{{us CoArticles}}{{\\left(t \\right)}} ={{}} \\sqrt[3]{{DP_{{CoArticles}} EV_{{us CoArticles}} TE_{{us CoArticles}}}}
  \\operatorname{{SR}}_{{us CoOre}}{{\\left(t \\right)}} ={{}} \\sqrt[3]{{DP_{{CoOre}} EV_{{us CoOre}} TE_{{us CoOre}}}}
  \\operatorname{{SR}}_{{us CoOxides}}{{\\left(t \\right)}} ={{}} \\sqrt[3]{{DP_{{CoOxides}} EV_{{us CoOxides}} TE_{{us CoOxides}}}}
  \\operatorname{{SR}}_{{us CoUnwrought}}{{\\left(t \\right)}} ={{}} \\sqrt[3]{{DP_{{CoUnwrought}} EV_{{us CoUnwrought}} TE_{{us CoUnwrought}}}}
  \\operatorname{{SV}}_{{CD CN}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CD}} - FWI_{{CN}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CD RW}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CD}} - FWI_{{RW}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CD SA}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CD}} - FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CD US}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CD}} - FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CD ZM}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CD}} - FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CN CD}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CD}} + FWI_{{CN}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CN RW}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CN}} - FWI_{{RW}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CN SA}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CN}} - FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CN US}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CN}} - FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{CN ZM}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{CN}} - FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{RW CD}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CD}} + FWI_{{RW}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{RW CN}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CN}} + FWI_{{RW}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{RW SA}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{RW}} - FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{RW US}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{RW}} - FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{RW ZM}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{RW}} - FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{SA CD}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CD}} + FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{SA CN}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CN}} + FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{SA RW}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{RW}} + FWI_{{SA}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{SA US}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{SA}} - FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{SA ZM}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{SA}} - FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{US CD}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CD}} + FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{US CN}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CN}} + FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{US RW}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{RW}} + FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{US SA}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{SA}} + FWI_{{US}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{US ZM}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(FWI_{{US}} - FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{ZM CD}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CD}} + FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{ZM CN}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{CN}} + FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{ZM RW}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{RW}} + FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{ZM SA}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{SA}} + FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{SV}}_{{ZM US}}{{\\left(t \\right)}} ={{}} \\sqrt{{\\left(- FWI_{{US}} + FWI_{{ZM}}\\right)^{{2}}}}
  \\operatorname{{TE}}_{{CD CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CD CoArticles}} + SP_{{CD CoArticles}}}}{{DeltaS_{{CD CoArticlesIE CD US CoArticles}} + IE_{{CD CN CoArticles}} + IE_{{CD RW CoArticles}} + IE_{{CD SA CoArticles}} + IE_{{CD ZM CoArticles}} - IE_{{CN CD CoArticles}} - IE_{{RW CD CoArticles}} - IE_{{SA CD CoArticles}} - IE_{{US CD CoArticles}} - IE_{{ZM CD CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{CD CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CD CoOre}} + SP_{{CD CoOre}}}}{{DeltaS_{{CD CoOreIE CD US CoOre}} + IE_{{CD CN CoOre}} + IE_{{CD RW CoOre}} + IE_{{CD SA CoOre}} + IE_{{CD ZM CoOre}} - IE_{{CN CD CoOre}} - IE_{{RW CD CoOre}} - IE_{{SA CD CoOre}} - IE_{{US CD CoOre}} - IE_{{ZM CD CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{CD CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CD CoOxides}} + SP_{{CD CoOxides}}}}{{DeltaS_{{CD CoOxidesIE CD US CoOxides}} + IE_{{CD CN CoOxides}} + IE_{{CD RW CoOxides}} + IE_{{CD SA CoOxides}} + IE_{{CD ZM CoOxides}} - IE_{{CN CD CoOxides}} - IE_{{RW CD CoOxides}} - IE_{{SA CD CoOxides}} - IE_{{US CD CoOxides}} - IE_{{ZM CD CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{CD CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CD CoUnwrought}} + SP_{{CD CoUnwrought}}}}{{DeltaS_{{CD CoUnwroughtIE CD US CoUnwrought}} + IE_{{CD CN CoUnwrought}} + IE_{{CD RW CoUnwrought}} + IE_{{CD SA CoUnwrought}} + IE_{{CD ZM CoUnwrought}} - IE_{{CN CD CoUnwrought}} - IE_{{RW CD CoUnwrought}} - IE_{{SA CD CoUnwrought}} - IE_{{US CD CoUnwrought}} - IE_{{ZM CD CoUnwrought}}}} + 1}}
  \\operatorname{{TE}}_{{CN CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CN CoArticles}} + SP_{{CN CoArticles}}}}{{DeltaS_{{CN CoArticlesIE CN US CoArticles}} - IE_{{CD CN CoArticles}} + IE_{{CN CD CoArticles}} + IE_{{CN RW CoArticles}} + IE_{{CN SA CoArticles}} + IE_{{CN ZM CoArticles}} - IE_{{RW CN CoArticles}} - IE_{{SA CN CoArticles}} - IE_{{US CN CoArticles}} - IE_{{ZM CN CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{CN CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CN CoOre}} + SP_{{CN CoOre}}}}{{DeltaS_{{CN CoOreIE CN US CoOre}} - IE_{{CD CN CoOre}} + IE_{{CN CD CoOre}} + IE_{{CN RW CoOre}} + IE_{{CN SA CoOre}} + IE_{{CN ZM CoOre}} - IE_{{RW CN CoOre}} - IE_{{SA CN CoOre}} - IE_{{US CN CoOre}} - IE_{{ZM CN CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{CN CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CN CoOxides}} + SP_{{CN CoOxides}}}}{{DeltaS_{{CN CoOxidesIE CN US CoOxides}} - IE_{{CD CN CoOxides}} + IE_{{CN CD CoOxides}} + IE_{{CN RW CoOxides}} + IE_{{CN SA CoOxides}} + IE_{{CN ZM CoOxides}} - IE_{{RW CN CoOxides}} - IE_{{SA CN CoOxides}} - IE_{{US CN CoOxides}} - IE_{{ZM CN CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{CN CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{CN CoUnwrought}} + SP_{{CN CoUnwrought}}}}{{DeltaS_{{CN CoUnwroughtIE CN US CoUnwrought}} - IE_{{CD CN CoUnwrought}} + IE_{{CN CD CoUnwrought}} + IE_{{CN RW CoUnwrought}} + IE_{{CN SA CoUnwrought}} + IE_{{CN ZM CoUnwrought}} - IE_{{RW CN CoUnwrought}} - IE_{{SA CN CoUnwrought}} - IE_{{US CN CoUnwrought}} - IE_{{ZM CN CoUnwrought}}}} + 1}}
  \\operatorname{{TE}}_{{RW CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{RW CoArticles}} + SP_{{RW CoArticles}}}}{{DeltaS_{{RW CoArticlesIE RW US CoArticles}} - IE_{{CD RW CoArticles}} - IE_{{CN RW CoArticles}} + IE_{{RW CD CoArticles}} + IE_{{RW CN CoArticles}} + IE_{{RW SA CoArticles}} + IE_{{RW ZM CoArticles}} - IE_{{SA RW CoArticles}} - IE_{{US RW CoArticles}} - IE_{{ZM RW CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{RW CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{RW CoOre}} + SP_{{RW CoOre}}}}{{DeltaS_{{RW CoOreIE RW US CoOre}} - IE_{{CD RW CoOre}} - IE_{{CN RW CoOre}} + IE_{{RW CD CoOre}} + IE_{{RW CN CoOre}} + IE_{{RW SA CoOre}} + IE_{{RW ZM CoOre}} - IE_{{SA RW CoOre}} - IE_{{US RW CoOre}} - IE_{{ZM RW CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{RW CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{RW CoOxides}} + SP_{{RW CoOxides}}}}{{DeltaS_{{RW CoOxidesIE RW US CoOxides}} - IE_{{CD RW CoOxides}} - IE_{{CN RW CoOxides}} + IE_{{RW CD CoOxides}} + IE_{{RW CN CoOxides}} + IE_{{RW SA CoOxides}} + IE_{{RW ZM CoOxides}} - IE_{{SA RW CoOxides}} - IE_{{US RW CoOxides}} - IE_{{ZM RW CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{RW CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{RW CoUnwrought}} + SP_{{RW CoUnwrought}}}}{{DeltaS_{{RW CoUnwroughtIE RW US CoUnwrought}} - IE_{{CD RW CoUnwrought}} - IE_{{CN RW CoUnwrought}} + IE_{{RW CD CoUnwrought}} + IE_{{RW CN CoUnwrought}} + IE_{{RW SA CoUnwrought}} + IE_{{RW ZM CoUnwrought}} - IE_{{SA RW CoUnwrought}} - IE_{{US RW CoUnwrought}} - IE_{{ZM RW CoUnwrought}}}} + 1}}
  \\operatorname{{TE}}_{{SA CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{SA CoArticles}} + SP_{{SA CoArticles}}}}{{DeltaS_{{SA CoArticlesIE SA US CoArticles}} - IE_{{CD SA CoArticles}} - IE_{{CN SA CoArticles}} - IE_{{RW SA CoArticles}} + IE_{{SA CD CoArticles}} + IE_{{SA CN CoArticles}} + IE_{{SA RW CoArticles}} + IE_{{SA ZM CoArticles}} - IE_{{US SA CoArticles}} - IE_{{ZM SA CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{SA CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{SA CoOre}} + SP_{{SA CoOre}}}}{{DeltaS_{{SA CoOreIE SA US CoOre}} - IE_{{CD SA CoOre}} - IE_{{CN SA CoOre}} - IE_{{RW SA CoOre}} + IE_{{SA CD CoOre}} + IE_{{SA CN CoOre}} + IE_{{SA RW CoOre}} + IE_{{SA ZM CoOre}} - IE_{{US SA CoOre}} - IE_{{ZM SA CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{SA CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{SA CoOxides}} + SP_{{SA CoOxides}}}}{{DeltaS_{{SA CoOxidesIE SA US CoOxides}} - IE_{{CD SA CoOxides}} - IE_{{CN SA CoOxides}} - IE_{{RW SA CoOxides}} + IE_{{SA CD CoOxides}} + IE_{{SA CN CoOxides}} + IE_{{SA RW CoOxides}} + IE_{{SA ZM CoOxides}} - IE_{{US SA CoOxides}} - IE_{{ZM SA CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{SA CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{SA CoUnwrought}} + SP_{{SA CoUnwrought}}}}{{DeltaS_{{SA CoUnwroughtIE SA US CoUnwrought}} - IE_{{CD SA CoUnwrought}} - IE_{{CN SA CoUnwrought}} - IE_{{RW SA CoUnwrought}} + IE_{{SA CD CoUnwrought}} + IE_{{SA CN CoUnwrought}} + IE_{{SA RW CoUnwrought}} + IE_{{SA ZM CoUnwrought}} - IE_{{US SA CoUnwrought}} - IE_{{ZM SA CoUnwrought}}}} + 1}}
  \\operatorname{{TE}}_{{US CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{US CoArticles}} + SP_{{US CoArticles}}}}{{DeltaS_{{US CoArticlesIE US CD CoArticles}} - IE_{{CD US CoArticles}} - IE_{{CN US CoArticles}} - IE_{{RW US CoArticles}} - IE_{{SA US CoArticles}} + IE_{{US CN CoArticles}} + IE_{{US RW CoArticles}} + IE_{{US SA CoArticles}} + IE_{{US ZM CoArticles}} - IE_{{ZM US CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{US CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{US CoOre}} + SP_{{US CoOre}}}}{{DeltaS_{{US CoOreIE US CD CoOre}} - IE_{{CD US CoOre}} - IE_{{CN US CoOre}} - IE_{{RW US CoOre}} - IE_{{SA US CoOre}} + IE_{{US CN CoOre}} + IE_{{US RW CoOre}} + IE_{{US SA CoOre}} + IE_{{US ZM CoOre}} - IE_{{ZM US CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{US CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{US CoOxides}} + SP_{{US CoOxides}}}}{{DeltaS_{{US CoOxidesIE US CD CoOxides}} - IE_{{CD US CoOxides}} - IE_{{CN US CoOxides}} - IE_{{RW US CoOxides}} - IE_{{SA US CoOxides}} + IE_{{US CN CoOxides}} + IE_{{US RW CoOxides}} + IE_{{US SA CoOxides}} + IE_{{US ZM CoOxides}} - IE_{{ZM US CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{US CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{US CoUnwrought}} + SP_{{US CoUnwrought}}}}{{DeltaS_{{US CoUnwroughtIE US CD CoUnwrought}} - IE_{{CD US CoUnwrought}} - IE_{{CN US CoUnwrought}} - IE_{{RW US CoUnwrought}} - IE_{{SA US CoUnwrought}} + IE_{{US CN CoUnwrought}} + IE_{{US RW CoUnwrought}} + IE_{{US SA CoUnwrought}} + IE_{{US ZM CoUnwrought}} - IE_{{ZM US CoUnwrought}}}} + 1}}
  \\operatorname{{TE}}_{{ZM CoArticles}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{ZM CoArticles}} + SP_{{ZM CoArticles}}}}{{DeltaS_{{ZM CoArticlesIE ZM US CoArticles}} - IE_{{CD ZM CoArticles}} - IE_{{CN ZM CoArticles}} - IE_{{RW ZM CoArticles}} - IE_{{SA ZM CoArticles}} - IE_{{US ZM CoArticles}} + IE_{{ZM CD CoArticles}} + IE_{{ZM CN CoArticles}} + IE_{{ZM RW CoArticles}} + IE_{{ZM SA CoArticles}}}} + 1}}
  \\operatorname{{TE}}_{{ZM CoOre}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{ZM CoOre}} + SP_{{ZM CoOre}}}}{{DeltaS_{{ZM CoOreIE ZM US CoOre}} - IE_{{CD ZM CoOre}} - IE_{{CN ZM CoOre}} - IE_{{RW ZM CoOre}} - IE_{{SA ZM CoOre}} - IE_{{US ZM CoOre}} + IE_{{ZM CD CoOre}} + IE_{{ZM CN CoOre}} + IE_{{ZM RW CoOre}} + IE_{{ZM SA CoOre}}}} + 1}}
  \\operatorname{{TE}}_{{ZM CoOxides}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{ZM CoOxides}} + SP_{{ZM CoOxides}}}}{{DeltaS_{{ZM CoOxidesIE ZM US CoOxides}} - IE_{{CD ZM CoOxides}} - IE_{{CN ZM CoOxides}} - IE_{{RW ZM CoOxides}} - IE_{{SA ZM CoOxides}} - IE_{{US ZM CoOxides}} + IE_{{ZM CD CoOxides}} + IE_{{ZM CN CoOxides}} + IE_{{ZM RW CoOxides}} + IE_{{ZM SA CoOxides}}}} + 1}}
  \\operatorname{{TE}}_{{ZM CoUnwrought}}{{\\left(t \\right)}} ={{}} \\frac{{1}}{{\\frac{{PP_{{ZM CoUnwrought}} + SP_{{ZM CoUnwrought}}}}{{DeltaS_{{ZM CoUnwroughtIE ZM US CoUnwrought}} - IE_{{CD ZM CoUnwrought}} - IE_{{CN ZM CoUnwrought}} - IE_{{RW ZM CoUnwrought}} - IE_{{SA ZM CoUnwrought}} - IE_{{US ZM CoUnwrought}} + IE_{{ZM CD CoUnwrought}} + IE_{{ZM CN CoUnwrought}} + IE_{{ZM RW CoUnwrought}} + IE_{{ZM SA CoUnwrought}}}} + 1}}
  \\operatorname{{TT}}_{{CD CN}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD CN CoArticles}} + IE_{{CD CN CoOre}} + IE_{{CD CN CoOxides}} + IE_{{CD CN CoUnwrought}} + IE_{{CN CD CoArticles}} + IE_{{CN CD CoOre}} + IE_{{CN CD CoOxides}} + IE_{{CN CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CD RW}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD RW CoArticles}} + IE_{{CD RW CoOre}} + IE_{{CD RW CoOxides}} + IE_{{CD RW CoUnwrought}} + IE_{{RW CD CoArticles}} + IE_{{RW CD CoOre}} + IE_{{RW CD CoOxides}} + IE_{{RW CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CD SA}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD SA CoArticles}} + IE_{{CD SA CoOre}} + IE_{{CD SA CoOxides}} + IE_{{CD SA CoUnwrought}} + IE_{{SA CD CoArticles}} + IE_{{SA CD CoOre}} + IE_{{SA CD CoOxides}} + IE_{{SA CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CD US}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD US CoArticles}} + IE_{{CD US CoOre}} + IE_{{CD US CoOxides}} + IE_{{CD US CoUnwrought}} + IE_{{US CD CoArticles}} + IE_{{US CD CoOre}} + IE_{{US CD CoOxides}} + IE_{{US CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CD ZM}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD ZM CoArticles}} + IE_{{CD ZM CoOre}} + IE_{{CD ZM CoOxides}} + IE_{{CD ZM CoUnwrought}} + IE_{{ZM CD CoArticles}} + IE_{{ZM CD CoOre}} + IE_{{ZM CD CoOxides}} + IE_{{ZM CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CD}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CN CD}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD CN CoArticles}} + IE_{{CD CN CoOre}} + IE_{{CD CN CoOxides}} + IE_{{CD CN CoUnwrought}} + IE_{{CN CD CoArticles}} + IE_{{CN CD CoOre}} + IE_{{CN CD CoOxides}} + IE_{{CN CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CN RW}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN RW CoArticles}} + IE_{{CN RW CoOre}} + IE_{{CN RW CoOxides}} + IE_{{CN RW CoUnwrought}} + IE_{{RW CN CoArticles}} + IE_{{RW CN CoOre}} + IE_{{RW CN CoOxides}} + IE_{{RW CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CN SA}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN SA CoArticles}} + IE_{{CN SA CoOre}} + IE_{{CN SA CoOxides}} + IE_{{CN SA CoUnwrought}} + IE_{{SA CN CoArticles}} + IE_{{SA CN CoOre}} + IE_{{SA CN CoOxides}} + IE_{{SA CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CN US}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN US CoArticles}} + IE_{{CN US CoOre}} + IE_{{CN US CoOxides}} + IE_{{CN US CoUnwrought}} + IE_{{US CN CoArticles}} + IE_{{US CN CoOre}} + IE_{{US CN CoOxides}} + IE_{{US CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{CN ZM}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN ZM CoArticles}} + IE_{{CN ZM CoOre}} + IE_{{CN ZM CoOxides}} + IE_{{CN ZM CoUnwrought}} + IE_{{ZM CN CoArticles}} + IE_{{ZM CN CoOre}} + IE_{{ZM CN CoOxides}} + IE_{{ZM CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{CN}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{RW CD}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD RW CoArticles}} + IE_{{CD RW CoOre}} + IE_{{CD RW CoOxides}} + IE_{{CD RW CoUnwrought}} + IE_{{RW CD CoArticles}} + IE_{{RW CD CoOre}} + IE_{{RW CD CoOxides}} + IE_{{RW CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{RW CN}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN RW CoArticles}} + IE_{{CN RW CoOre}} + IE_{{CN RW CoOxides}} + IE_{{CN RW CoUnwrought}} + IE_{{RW CN CoArticles}} + IE_{{RW CN CoOre}} + IE_{{RW CN CoOxides}} + IE_{{RW CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{RW SA}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW SA CoArticles}} + IE_{{RW SA CoOre}} + IE_{{RW SA CoOxides}} + IE_{{RW SA CoUnwrought}} + IE_{{SA RW CoArticles}} + IE_{{SA RW CoOre}} + IE_{{SA RW CoOxides}} + IE_{{SA RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{RW US}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW US CoArticles}} + IE_{{RW US CoOre}} + IE_{{RW US CoOxides}} + IE_{{RW US CoUnwrought}} + IE_{{US RW CoArticles}} + IE_{{US RW CoOre}} + IE_{{US RW CoOxides}} + IE_{{US RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{RW ZM}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW ZM CoArticles}} + IE_{{RW ZM CoOre}} + IE_{{RW ZM CoOxides}} + IE_{{RW ZM CoUnwrought}} + IE_{{ZM RW CoArticles}} + IE_{{ZM RW CoOre}} + IE_{{ZM RW CoOxides}} + IE_{{ZM RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{RW}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{SA CD}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD SA CoArticles}} + IE_{{CD SA CoOre}} + IE_{{CD SA CoOxides}} + IE_{{CD SA CoUnwrought}} + IE_{{SA CD CoArticles}} + IE_{{SA CD CoOre}} + IE_{{SA CD CoOxides}} + IE_{{SA CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{SA CN}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN SA CoArticles}} + IE_{{CN SA CoOre}} + IE_{{CN SA CoOxides}} + IE_{{CN SA CoUnwrought}} + IE_{{SA CN CoArticles}} + IE_{{SA CN CoOre}} + IE_{{SA CN CoOxides}} + IE_{{SA CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{SA RW}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW SA CoArticles}} + IE_{{RW SA CoOre}} + IE_{{RW SA CoOxides}} + IE_{{RW SA CoUnwrought}} + IE_{{SA RW CoArticles}} + IE_{{SA RW CoOre}} + IE_{{SA RW CoOxides}} + IE_{{SA RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{SA US}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{SA US CoArticles}} + IE_{{SA US CoOre}} + IE_{{SA US CoOxides}} + IE_{{SA US CoUnwrought}} + IE_{{US SA CoArticles}} + IE_{{US SA CoOre}} + IE_{{US SA CoOxides}} + IE_{{US SA CoUnwrought}}}}{{\\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{SA ZM}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{SA ZM CoArticles}} + IE_{{SA ZM CoOre}} + IE_{{SA ZM CoOxides}} + IE_{{SA ZM CoUnwrought}} + IE_{{ZM SA CoArticles}} + IE_{{ZM SA CoOre}} + IE_{{ZM SA CoOxides}} + IE_{{ZM SA CoUnwrought}}}}{{\\operatorname{{GDP}}_{{SA}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{US CD}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD US CoArticles}} + IE_{{CD US CoOre}} + IE_{{CD US CoOxides}} + IE_{{CD US CoUnwrought}} + IE_{{US CD CoArticles}} + IE_{{US CD CoOre}} + IE_{{US CD CoOxides}} + IE_{{US CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{US CN}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN US CoArticles}} + IE_{{CN US CoOre}} + IE_{{CN US CoOxides}} + IE_{{CN US CoUnwrought}} + IE_{{US CN CoArticles}} + IE_{{US CN CoOre}} + IE_{{US CN CoOxides}} + IE_{{US CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{US RW}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW US CoArticles}} + IE_{{RW US CoOre}} + IE_{{RW US CoOxides}} + IE_{{RW US CoUnwrought}} + IE_{{US RW CoArticles}} + IE_{{US RW CoOre}} + IE_{{US RW CoOxides}} + IE_{{US RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{US SA}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{SA US CoArticles}} + IE_{{SA US CoOre}} + IE_{{SA US CoOxides}} + IE_{{SA US CoUnwrought}} + IE_{{US SA CoArticles}} + IE_{{US SA CoOre}} + IE_{{US SA CoOxides}} + IE_{{US SA CoUnwrought}}}}{{\\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{US ZM}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{US ZM CoArticles}} + IE_{{US ZM CoOre}} + IE_{{US ZM CoOxides}} + IE_{{US ZM CoUnwrought}} + IE_{{ZM US CoArticles}} + IE_{{ZM US CoOre}} + IE_{{ZM US CoOxides}} + IE_{{ZM US CoUnwrought}}}}{{\\operatorname{{GDP}}_{{US}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{ZM CD}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CD ZM CoArticles}} + IE_{{CD ZM CoOre}} + IE_{{CD ZM CoOxides}} + IE_{{CD ZM CoUnwrought}} + IE_{{ZM CD CoArticles}} + IE_{{ZM CD CoOre}} + IE_{{ZM CD CoOxides}} + IE_{{ZM CD CoUnwrought}}}}{{\\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{ZM CN}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{CN ZM CoArticles}} + IE_{{CN ZM CoOre}} + IE_{{CN ZM CoOxides}} + IE_{{CN ZM CoUnwrought}} + IE_{{ZM CN CoArticles}} + IE_{{ZM CN CoOre}} + IE_{{ZM CN CoOxides}} + IE_{{ZM CN CoUnwrought}}}}{{\\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{ZM RW}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{RW ZM CoArticles}} + IE_{{RW ZM CoOre}} + IE_{{RW ZM CoOxides}} + IE_{{RW ZM CoUnwrought}} + IE_{{ZM RW CoArticles}} + IE_{{ZM RW CoOre}} + IE_{{ZM RW CoOxides}} + IE_{{ZM RW CoUnwrought}}}}{{\\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{ZM SA}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{SA ZM CoArticles}} + IE_{{SA ZM CoOre}} + IE_{{SA ZM CoOxides}} + IE_{{SA ZM CoUnwrought}} + IE_{{ZM SA CoArticles}} + IE_{{ZM SA CoOre}} + IE_{{ZM SA CoOxides}} + IE_{{ZM SA CoUnwrought}}}}{{\\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}}}
  \\operatorname{{TT}}_{{ZM US}}{{\\left(t \\right)}} ={{}} \\frac{{IE_{{US ZM CoArticles}} + IE_{{US ZM CoOre}} + IE_{{US ZM CoOxides}} + IE_{{US ZM CoUnwrought}} + IE_{{ZM US CoArticles}} + IE_{{ZM US CoOre}} + IE_{{ZM US CoOxides}} + IE_{{ZM US CoUnwrought}}}}{{\\operatorname{{GDP}}_{{ZM}}{{\\left(t \\right)}}}}
  \\operatorname{{WSI}}_{{CD CN}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CD CN}}}}{{2}} + \\frac{{TT_{{CD CN}}}}{{2}}
  \\operatorname{{WSI}}_{{CD RW}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CD RW}}}}{{2}} + \\frac{{TT_{{CD RW}}}}{{2}}
  \\operatorname{{WSI}}_{{CD SA}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CD SA}}}}{{2}} + \\frac{{TT_{{CD SA}}}}{{2}}
  \\operatorname{{WSI}}_{{CD US}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CD US}}}}{{2}} + \\frac{{TT_{{CD US}}}}{{2}}
  \\operatorname{{WSI}}_{{CD ZM}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CD ZM}}}}{{2}} + \\frac{{TT_{{CD ZM}}}}{{2}}
  \\operatorname{{WSI}}_{{CN CD}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CN CD}}}}{{2}} + \\frac{{TT_{{CN CD}}}}{{2}}
  \\operatorname{{WSI}}_{{CN RW}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CN RW}}}}{{2}} + \\frac{{TT_{{CN RW}}}}{{2}}
  \\operatorname{{WSI}}_{{CN SA}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CN SA}}}}{{2}} + \\frac{{TT_{{CN SA}}}}{{2}}
  \\operatorname{{WSI}}_{{CN US}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CN US}}}}{{2}} + \\frac{{TT_{{CN US}}}}{{2}}
  \\operatorname{{WSI}}_{{CN ZM}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{CN ZM}}}}{{2}} + \\frac{{TT_{{CN ZM}}}}{{2}}
  \\operatorname{{WSI}}_{{RW CD}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{RW CD}}}}{{2}} + \\frac{{TT_{{RW CD}}}}{{2}}
  \\operatorname{{WSI}}_{{RW CN}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{RW CN}}}}{{2}} + \\frac{{TT_{{RW CN}}}}{{2}}
  \\operatorname{{WSI}}_{{RW SA}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{RW SA}}}}{{2}} + \\frac{{TT_{{RW SA}}}}{{2}}
  \\operatorname{{WSI}}_{{RW US}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{RW US}}}}{{2}} + \\frac{{TT_{{RW US}}}}{{2}}
  \\operatorname{{WSI}}_{{RW ZM}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{RW ZM}}}}{{2}} + \\frac{{TT_{{RW ZM}}}}{{2}}
  \\operatorname{{WSI}}_{{SA CD}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{SA CD}}}}{{2}} + \\frac{{TT_{{SA CD}}}}{{2}}
  \\operatorname{{WSI}}_{{SA CN}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{SA CN}}}}{{2}} + \\frac{{TT_{{SA CN}}}}{{2}}
  \\operatorname{{WSI}}_{{SA RW}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{SA RW}}}}{{2}} + \\frac{{TT_{{SA RW}}}}{{2}}
  \\operatorname{{WSI}}_{{SA US}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{SA US}}}}{{2}} + \\frac{{TT_{{SA US}}}}{{2}}
  \\operatorname{{WSI}}_{{SA ZM}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{SA ZM}}}}{{2}} + \\frac{{TT_{{SA ZM}}}}{{2}}
  \\operatorname{{WSI}}_{{US CD}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{US CD}}}}{{2}} + \\frac{{TT_{{US CD}}}}{{2}}
  \\operatorname{{WSI}}_{{US CN}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{US CN}}}}{{2}} + \\frac{{TT_{{US CN}}}}{{2}}
  \\operatorname{{WSI}}_{{US RW}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{US RW}}}}{{2}} + \\frac{{TT_{{US RW}}}}{{2}}
  \\operatorname{{WSI}}_{{US SA}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{US SA}}}}{{2}} + \\frac{{TT_{{US SA}}}}{{2}}
  \\operatorname{{WSI}}_{{US ZM}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{US ZM}}}}{{2}} + \\frac{{TT_{{US ZM}}}}{{2}}
  \\operatorname{{WSI}}_{{ZM CD}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{ZM CD}}}}{{2}} + \\frac{{TT_{{ZM CD}}}}{{2}}
  \\operatorname{{WSI}}_{{ZM CN}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{ZM CN}}}}{{2}} + \\frac{{TT_{{ZM CN}}}}{{2}}
  \\operatorname{{WSI}}_{{ZM RW}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{ZM RW}}}}{{2}} + \\frac{{TT_{{ZM RW}}}}{{2}}
  \\operatorname{{WSI}}_{{ZM SA}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{ZM SA}}}}{{2}} + \\frac{{TT_{{ZM SA}}}}{{2}}
  \\operatorname{{WSI}}_{{ZM US}}{{\\left(t \\right)}} ={{}} \\frac{{SV_{{ZM US}}}}{{2}} + \\frac{{TT_{{ZM US}}}}{{2}}
\\end{{align}}


The tunable paramters, along with their descriptions purposes, are listed here in JSON-format:

--- PARAMETERS START ---
{parameters}

[
        {{
          "id": "IE_CD_US_CoArticles",
          "description": "Trade from CD to US"
        }},
        {{
          "id": "IE_CD_US_CoOre",
          "description": "Trade from CD to US"
        }},
        {{
          "id": "IE_CD_US_CoOxides",
          "description": "Trade from CD to US"
        }},
        {{
          "id": "IE_CD_US_CoUnwrought",
          "description": "Trade from CD to US"
        }},
        {{
          "id": "IE_US_CD_CoArticles",
          "description": "Trade from US to CD"
        }},
        {{
          "id": "IE_US_CD_CoOre",
          "description": "Trade from US to CD"
        }},
        {{
          "id": "IE_US_CD_CoOxides",
          "description": "Trade from US to CD"
        }},
        {{
          "id": "IE_US_CD_CoUnwrought",
          "description": "Trade from US to CD"
        }},
        {{
          "id": "IE_US_ZM_CoArticles",
          "description": "Trade from US to ZM"
        }},
        {{
          "id": "IE_US_ZM_CoOre",
          "description": "Trade from US to ZM"
        }},
        {{
          "id": "IE_US_ZM_CoOxides",
          "description": "Trade from US to ZM"
        }},
        {{
          "id": "IE_US_ZM_CoUnwrought",
          "description": "Trade from US to ZM"
        }},
        {{
          "id": "IE_ZM_US_CoArticles",
          "description": "Trade from ZM to US"
        }},
        {{
          "id": "IE_ZM_US_CoOre",
          "description": "Trade from ZM to US"
        }},
        {{
          "id": "IE_ZM_US_CoOxides",
          "description": "Trade from ZM to US"
        }},
        {{
          "id": "IE_ZM_US_CoUnwrought",
          "description": "Trade from ZM to US"
        }},
        {{
          "id": "IE_SA_US_CoArticles",
          "description": "Trade from SA to US"
        }},
        {{
          "id": "IE_SA_US_CoOre",
          "description": "Trade from SA to US"
        }},
        {{
          "id": "IE_SA_US_CoOxides",
          "description": "Trade from SA to US"
        }},
        {{
          "id": "IE_SA_US_CoUnwrought",
          "description": "Trade from SA to US"
        }},
        {{
          "id": "IE_US_SA_CoArticles",
          "description": "Trade from US to SA"
        }},
        {{
          "id": "IE_US_SA_CoOre",
          "description": "Trade from US to SA"
        }},
        {{
          "id": "IE_US_SA_CoOxides",
          "description": "Trade from US to SA"
        }},
        {{
          "id": "IE_US_SA_CoUnwrought",
          "description": "Trade from US to SA"
        }},
        {{
          "id": "IE_RW_US_CoArticles",
          "description": "Trade from RW to US"
        }},
        {{
          "id": "IE_RW_US_CoOre",
          "description": "Trade from RW to US"
        }},
        {{
          "id": "IE_RW_US_CoOxides",
          "description": "Trade from RW to US"
        }},
        {{
          "id": "IE_RW_US_CoUnwrought",
          "description": "Trade from RW to US"
        }},
        {{
          "id": "IE_US_RW_CoArticles",
          "description": "Trade from US to RW"
        }},
        {{
          "id": "IE_US_RW_CoOre",
          "description": "Trade from US to RW"
        }},
        {{
          "id": "IE_US_RW_CoOxides",
          "description": "Trade from US to RW"
        }},
        {{
          "id": "IE_US_RW_CoUnwrought",
          "description": "Trade from US to RW"
        }},
        {{
          "id": "IE_CN_US_CoArticles",
          "description": "Trade from CN to US"
        }},
        {{
          "id": "IE_CN_US_CoOre",
          "description": "Trade from CN to US"
        }},
        {{
          "id": "IE_CN_US_CoOxides",
          "description": "Trade from CN to US"
        }},
        {{
          "id": "IE_CN_US_CoUnwrought",
          "description": "Trade from CN to US"
        }},
        {{
          "id": "IE_US_CN_CoArticles",
          "description": "Trade from US to CN"
        }},
        {{
          "id": "IE_US_CN_CoOre",
          "description": "Trade from US to CN"
        }},
        {{
          "id": "IE_US_CN_CoOxides",
          "description": "Trade from US to CN"
        }},
        {{
          "id": "IE_US_CN_CoUnwrought",
          "description": "Trade from US to CN"
        }},
        {{
          "id": "IE_CD_ZM_CoArticles",
          "description": "Trade from CD to ZM"
        }},
        {{
          "id": "IE_CD_ZM_CoOre",
          "description": "Trade from CD to ZM"
        }},
        {{
          "id": "IE_CD_ZM_CoOxides",
          "description": "Trade from CD to ZM"
        }},
        {{
          "id": "IE_CD_ZM_CoUnwrought",
          "description": "Trade from CD to ZM"
        }},
        {{
          "id": "IE_ZM_CD_CoArticles",
          "description": "Trade from ZM to CD"
        }},
        {{
          "id": "IE_ZM_CD_CoOre",
          "description": "Trade from ZM to CD"
        }},
        {{
          "id": "IE_ZM_CD_CoOxides",
          "description": "Trade from ZM to CD"
        }},
        {{
          "id": "IE_ZM_CD_CoUnwrought",
          "description": "Trade from ZM to CD"
        }},
        {{
          "id": "IE_CD_SA_CoArticles",
          "description": "Trade from CD to SA"
        }},
        {{
          "id": "IE_CD_SA_CoOre",
          "description": "Trade from CD to SA"
        }},
        {{
          "id": "IE_CD_SA_CoOxides",
          "description": "Trade from CD to SA"
        }},
        {{
          "id": "IE_CD_SA_CoUnwrought",
          "description": "Trade from CD to SA"
        }},
        {{
          "id": "IE_SA_CD_CoArticles",
          "description": "Trade from SA to CD"
        }},
        {{
          "id": "IE_SA_CD_CoOre",
          "description": "Trade from SA to CD"
        }},
        {{
          "id": "IE_SA_CD_CoOxides",
          "description": "Trade from SA to CD"
        }},
        {{
          "id": "IE_SA_CD_CoUnwrought",
          "description": "Trade from SA to CD"
        }},
        {{
          "id": "IE_CD_RW_CoArticles",
          "description": "Trade from CD to RW"
        }},
        {{
          "id": "IE_CD_RW_CoOre",
          "description": "Trade from CD to RW"
        }},
        {{
          "id": "IE_CD_RW_CoOxides",
          "description": "Trade from CD to RW"
        }},
        {{
          "id": "IE_CD_RW_CoUnwrought",
          "description": "Trade from CD to RW"
        }},
        {{
          "id": "IE_RW_CD_CoArticles",
          "description": "Trade from RW to CD"
        }},
        {{
          "id": "IE_RW_CD_CoOre",
          "description": "Trade from RW to CD"
        }},
        {{
          "id": "IE_RW_CD_CoOxides",
          "description": "Trade from RW to CD"
        }},
        {{
          "id": "IE_RW_CD_CoUnwrought",
          "description": "Trade from RW to CD"
        }},
        {{
          "id": "IE_CD_CN_CoArticles",
          "description": "Trade from CD to CN"
        }},
        {{
          "id": "IE_CD_CN_CoOre",
          "description": "Trade from CD to CN"
        }},
        {{
          "id": "IE_CD_CN_CoOxides",
          "description": "Trade from CD to CN"
        }},
        {{
          "id": "IE_CD_CN_CoUnwrought",
          "description": "Trade from CD to CN"
        }},
        {{
          "id": "IE_CN_CD_CoArticles",
          "description": "Trade from CN to CD"
        }},
        {{
          "id": "IE_CN_CD_CoOre",
          "description": "Trade from CN to CD"
        }},
        {{
          "id": "IE_CN_CD_CoOxides",
          "description": "Trade from CN to CD"
        }},
        {{
          "id": "IE_CN_CD_CoUnwrought",
          "description": "Trade from CN to CD"
        }},
        {{
          "id": "IE_SA_ZM_CoArticles",
          "description": "Trade from SA to ZM"
        }},
        {{
          "id": "IE_SA_ZM_CoOre",
          "description": "Trade from SA to ZM"
        }},
        {{
          "id": "IE_SA_ZM_CoOxides",
          "description": "Trade from SA to ZM"
        }},
        {{
          "id": "IE_SA_ZM_CoUnwrought",
          "description": "Trade from SA to ZM"
        }},
        {{
          "id": "IE_ZM_SA_CoArticles",
          "description": "Trade from ZM to SA"
        }},
        {{
          "id": "IE_ZM_SA_CoOre",
          "description": "Trade from ZM to SA"
        }},
        {{
          "id": "IE_ZM_SA_CoOxides",
          "description": "Trade from ZM to SA"
        }},
        {{
          "id": "IE_ZM_SA_CoUnwrought",
          "description": "Trade from ZM to SA"
        }},
        {{
          "id": "IE_RW_ZM_CoArticles",
          "description": "Trade from RW to ZM"
        }},
        {{
          "id": "IE_RW_ZM_CoOre",
          "description": "Trade from RW to ZM"
        }},
        {{
          "id": "IE_RW_ZM_CoOxides",
          "description": "Trade from RW to ZM"
        }},
        {{
          "id": "IE_RW_ZM_CoUnwrought",
          "description": "Trade from RW to ZM"
        }},
        {{
          "id": "IE_ZM_RW_CoArticles",
          "description": "Trade from ZM to RW"
        }},
        {{
          "id": "IE_ZM_RW_CoOre",
          "description": "Trade from ZM to RW"
        }},
        {{
          "id": "IE_ZM_RW_CoOxides",
          "description": "Trade from ZM to RW"
        }},
        {{
          "id": "IE_ZM_RW_CoUnwrought",
          "description": "Trade from ZM to RW"
        }},
        {{
          "id": "IE_CN_ZM_CoArticles",
          "description": "Trade from CN to ZM"
        }},
        {{
          "id": "IE_CN_ZM_CoOre",
          "description": "Trade from CN to ZM"
        }},
        {{
          "id": "IE_CN_ZM_CoOxides",
          "description": "Trade from CN to ZM"
        }},
        {{
          "id": "IE_CN_ZM_CoUnwrought",
          "description": "Trade from CN to ZM"
        }},
        {{
          "id": "IE_ZM_CN_CoArticles",
          "description": "Trade from ZM to CN"
        }},
        {{
          "id": "IE_ZM_CN_CoOre",
          "description": "Trade from ZM to CN"
        }},
        {{
          "id": "IE_ZM_CN_CoOxides",
          "description": "Trade from ZM to CN"
        }},
        {{
          "id": "IE_ZM_CN_CoUnwrought",
          "description": "Trade from ZM to CN"
        }},
        {{
          "id": "IE_RW_SA_CoArticles",
          "description": "Trade from RW to SA"
        }},
        {{
          "id": "IE_RW_SA_CoOre",
          "description": "Trade from RW to SA"
        }},
        {{
          "id": "IE_RW_SA_CoOxides",
          "description": "Trade from RW to SA"
        }},
        {{
          "id": "IE_RW_SA_CoUnwrought",
          "description": "Trade from RW to SA"
        }},
        {{
          "id": "IE_SA_RW_CoArticles",
          "description": "Trade from SA to RW"
        }},
        {{
          "id": "IE_SA_RW_CoOre",
          "description": "Trade from SA to RW"
        }},
        {{
          "id": "IE_SA_RW_CoOxides",
          "description": "Trade from SA to RW"
        }},
        {{
          "id": "IE_SA_RW_CoUnwrought",
          "description": "Trade from SA to RW"
        }},
        {{
          "id": "IE_CN_SA_CoArticles",
          "description": "Trade from CN to SA"
        }},
        {{
          "id": "IE_CN_SA_CoOre",
          "description": "Trade from CN to SA"
        }},
        {{
          "id": "IE_CN_SA_CoOxides",
          "description": "Trade from CN to SA"
        }},
        {{
          "id": "IE_CN_SA_CoUnwrought",
          "description": "Trade from CN to SA"
        }},
        {{
          "id": "IE_SA_CN_CoArticles",
          "description": "Trade from SA to CN"
        }},
        {{
          "id": "IE_SA_CN_CoOre",
          "description": "Trade from SA to CN"
        }},
        {{
          "id": "IE_SA_CN_CoOxides",
          "description": "Trade from SA to CN"
        }},
        {{
          "id": "IE_SA_CN_CoUnwrought",
          "description": "Trade from SA to CN"
        }},
        {{
          "id": "IE_CN_RW_CoArticles",
          "description": "Trade from CN to RW"
        }},
        {{
          "id": "IE_CN_RW_CoOre",
          "description": "Trade from CN to RW"
        }},
        {{
          "id": "IE_CN_RW_CoOxides",
          "description": "Trade from CN to RW"
        }},
        {{
          "id": "IE_CN_RW_CoUnwrought",
          "description": "Trade from CN to RW"
        }},
        {{
          "id": "IE_RW_CN_CoArticles",
          "description": "Trade from RW to CN"
        }},
        {{
          "id": "IE_RW_CN_CoOre",
          "description": "Trade from RW to CN"
        }},
        {{
          "id": "IE_RW_CN_CoOxides",
          "description": "Trade from RW to CN"
        }},
        {{
          "id": "IE_RW_CN_CoUnwrought",
          "description": "Trade from RW to CN"
        }},
        {{
          "id": "FWI_CD",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "FWI_US",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "FWI_ZM",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "FWI_SA",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "FWI_RW",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "FWI_CN",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_US",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_CD",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_ZM",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_SA",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_RW",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PPI_CN",
          "description": "Policy perception index of a country US CD ZM SA RW CN."
        }},
        {{
          "id": "PP_CD_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "PP_CN_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "PP_RW_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "PP_SA_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "PP_US_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "PP_ZM_CoArticles",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CD_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "SP_CN_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "SP_RW_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "SP_SA_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "SP_US_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "SP_ZM_CoArticles",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_US_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_US_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_US_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_US_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_US_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_US_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CD_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CD_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CD_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CD_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CD_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CD_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_ZM_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_ZM_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_ZM_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_ZM_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_ZM_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_ZM_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_SA_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_SA_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_SA_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_SA_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_SA_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_SA_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_RW_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_RW_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_RW_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_RW_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_RW_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_RW_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CN_CoOre",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CN_CoOre",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CN_CoOxides",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CN_CoOxides",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "PP_CN_CoUnwrought",
          "description": "Primary production of a commodity in a country."
        }},
        {{
          "id": "SP_CN_CoUnwrought",
          "description": "Secondary production of a commodity in a country."
        }},
        {{
          "id": "EXP_magneticalloys_CoOre",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_steels_CoOre",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_superalloys_CoOre",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "OP_magneticalloys",
          "description": "Operating profit of an industry in the US."
        }},
        {{
          "id": "OP_steels",
          "description": "Operating profit of an industry in the US."
        }},
        {{
          "id": "OP_superalloys",
          "description": "Operating profit of an industry in the US."
        }},
        {{
          "id": "VA_magneticalloys",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US."
        }},
        {{
          "id": "VA_steels",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US."
        }},
        {{
          "id": "VA_superalloys",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US."
        }},
        {{
          "id": "EXP_magneticalloys_CoOxides",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_steels_CoOxides",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_superalloys_CoOxides",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_magneticalloys_CoUnwrought",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_steels_CoUnwrought",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_superalloys_CoUnwrought",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_magneticalloys_CoArticles",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_steels_CoArticles",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "EXP_superalloys_CoArticles",
          "description": "Expenditure of an industry on a commodity in the US."
        }},
        {{
          "id": "DeltaS_US_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_US_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_US_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_US_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CD_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CD_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CD_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CD_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_ZM_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_ZM_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_ZM_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_ZM_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_SA_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_SA_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_SA_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_SA_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_RW_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_RW_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_RW_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_RW_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CN_CoOre",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CN_CoOxides",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CN_CoUnwrought",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "DeltaS_CN_CoArticles",
          "description": "Adjustments of industry and government stocks of a commodity in a country."
        }},
        {{
          "id": "g_US",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "g_CD",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "g_ZM",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "g_SA",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "g_RW",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "g_CN",
          "description": "GDO growth rate of a country."
        }},
        {{
          "id": "MC_US",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }},
        {{
          "id": "MC_CD",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }},
        {{
          "id": "MC_ZM",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }},
        {{
          "id": "MC_SA",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }},
        {{
          "id": "MC_RW",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }},
        {{
          "id": "MC_CN",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN."
        }}
      ]

--- PARAMETERS END ---


Compose your reply with up to 4 top answers, with this JSON-format:

{{
  "answer": [
    {{ "id": <parameter_id>, "reason": <reason> }},
    {{ "id": <parameter_id>, "reason": <reason> }},
    ...
  ]
}}


If there are no good answers, return answer as an empty array.


The question is:

{question}

"""
