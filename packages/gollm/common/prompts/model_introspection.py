MODEL_INTROSPECTION_PROMPT = """
You are an expert in global trades, supply chains, and geopolitics.
Your job is to assess which part of the model can be tweaks and tuned to answer our question below.


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
\\end{{align}}


The tunable paramters, along with their descriptions purposes, are listed here in JSON-format:

--- PARAMETERS START ---
{parameters}

[
        {{
          "id": "IE_CD_US_CoArticles",
          "name": "IE_CD_US",
          "description": "Trade from CD to US",
          "value": 0,
          "conceptReference": "IE_CD_US_CoArticles"
        }},
        {{
          "id": "IE_CD_US_CoOre",
          "name": "IE_CD_US",
          "description": "Trade from CD to US",
          "value": 0,
          "conceptReference": "IE_CD_US_CoOre"
        }},
        {{
          "id": "IE_CD_US_CoOxides",
          "name": "IE_CD_US",
          "description": "Trade from CD to US",
          "value": 0,
          "conceptReference": "IE_CD_US_CoOxides"
        }},
        {{
          "id": "IE_CD_US_CoUnwrought",
          "name": "IE_CD_US",
          "description": "Trade from CD to US",
          "value": 0,
          "conceptReference": "IE_CD_US_CoUnwrought"
        }},
        {{
          "id": "IE_US_CD_CoArticles",
          "name": "IE_US_CD",
          "description": "Trade from US to CD",
          "value": 0,
          "conceptReference": "IE_US_CD_CoArticles"
        }},
        {{
          "id": "IE_US_CD_CoOre",
          "name": "IE_US_CD",
          "description": "Trade from US to CD",
          "value": 0,
          "conceptReference": "IE_US_CD_CoOre"
        }},
        {{
          "id": "IE_US_CD_CoOxides",
          "name": "IE_US_CD",
          "description": "Trade from US to CD",
          "value": 0,
          "conceptReference": "IE_US_CD_CoOxides"
        }},
        {{
          "id": "IE_US_CD_CoUnwrought",
          "name": "IE_US_CD",
          "description": "Trade from US to CD",
          "value": 0,
          "conceptReference": "IE_US_CD_CoUnwrought"
        }},
        {{
          "id": "IE_US_ZM_CoArticles",
          "name": "IE_US_ZM",
          "description": "Trade from US to ZM",
          "value": 0,
          "conceptReference": "IE_US_ZM_CoArticles"
        }},
        {{
          "id": "IE_US_ZM_CoOre",
          "name": "IE_US_ZM",
          "description": "Trade from US to ZM",
          "value": 0,
          "conceptReference": "IE_US_ZM_CoOre"
        }},
        {{
          "id": "IE_US_ZM_CoOxides",
          "name": "IE_US_ZM",
          "description": "Trade from US to ZM",
          "value": 0,
          "conceptReference": "IE_US_ZM_CoOxides"
        }},
        {{
          "id": "IE_US_ZM_CoUnwrought",
          "name": "IE_US_ZM",
          "description": "Trade from US to ZM",
          "value": 0,
          "conceptReference": "IE_US_ZM_CoUnwrought"
        }},
        {{
          "id": "IE_ZM_US_CoArticles",
          "name": "IE_ZM_US",
          "description": "Trade from ZM to US",
          "value": 0,
          "conceptReference": "IE_ZM_US_CoArticles"
        }},
        {{
          "id": "IE_ZM_US_CoOre",
          "name": "IE_ZM_US",
          "description": "Trade from ZM to US",
          "value": 0,
          "conceptReference": "IE_ZM_US_CoOre"
        }},
        {{
          "id": "IE_ZM_US_CoOxides",
          "name": "IE_ZM_US",
          "description": "Trade from ZM to US",
          "value": 0,
          "conceptReference": "IE_ZM_US_CoOxides"
        }},
        {{
          "id": "IE_ZM_US_CoUnwrought",
          "name": "IE_ZM_US",
          "description": "Trade from ZM to US",
          "value": 0,
          "conceptReference": "IE_ZM_US_CoUnwrought"
        }},
        {{
          "id": "IE_SA_US_CoArticles",
          "name": "IE_SA_US",
          "description": "Trade from SA to US",
          "value": 0,
          "conceptReference": "IE_SA_US_CoArticles"
        }},
        {{
          "id": "IE_SA_US_CoOre",
          "name": "IE_SA_US",
          "description": "Trade from SA to US",
          "value": 0,
          "conceptReference": "IE_SA_US_CoOre"
        }},
        {{
          "id": "IE_SA_US_CoOxides",
          "name": "IE_SA_US",
          "description": "Trade from SA to US",
          "value": 0,
          "conceptReference": "IE_SA_US_CoOxides"
        }},
        {{
          "id": "IE_SA_US_CoUnwrought",
          "name": "IE_SA_US",
          "description": "Trade from SA to US",
          "value": 0,
          "conceptReference": "IE_SA_US_CoUnwrought"
        }},
        {{
          "id": "IE_US_SA_CoArticles",
          "name": "IE_US_SA",
          "description": "Trade from US to SA",
          "value": 0,
          "conceptReference": "IE_US_SA_CoArticles"
        }},
        {{
          "id": "IE_US_SA_CoOre",
          "name": "IE_US_SA",
          "description": "Trade from US to SA",
          "value": 0,
          "conceptReference": "IE_US_SA_CoOre"
        }},
        {{
          "id": "IE_US_SA_CoOxides",
          "name": "IE_US_SA",
          "description": "Trade from US to SA",
          "value": 0,
          "conceptReference": "IE_US_SA_CoOxides"
        }},
        {{
          "id": "IE_US_SA_CoUnwrought",
          "name": "IE_US_SA",
          "description": "Trade from US to SA",
          "value": 0,
          "conceptReference": "IE_US_SA_CoUnwrought"
        }},
        {{
          "id": "IE_RW_US_CoArticles",
          "name": "IE_RW_US",
          "description": "Trade from RW to US",
          "value": 0,
          "conceptReference": "IE_RW_US_CoArticles"
        }},
        {{
          "id": "IE_RW_US_CoOre",
          "name": "IE_RW_US",
          "description": "Trade from RW to US",
          "value": 0,
          "conceptReference": "IE_RW_US_CoOre"
        }},
        {{
          "id": "IE_RW_US_CoOxides",
          "name": "IE_RW_US",
          "description": "Trade from RW to US",
          "value": 0,
          "conceptReference": "IE_RW_US_CoOxides"
        }},
        {{
          "id": "IE_RW_US_CoUnwrought",
          "name": "IE_RW_US",
          "description": "Trade from RW to US",
          "value": 0,
          "conceptReference": "IE_RW_US_CoUnwrought"
        }},
        {{
          "id": "IE_US_RW_CoArticles",
          "name": "IE_US_RW",
          "description": "Trade from US to RW",
          "value": 0,
          "conceptReference": "IE_US_RW_CoArticles"
        }},
        {{
          "id": "IE_US_RW_CoOre",
          "name": "IE_US_RW",
          "description": "Trade from US to RW",
          "value": 0,
          "conceptReference": "IE_US_RW_CoOre"
        }},
        {{
          "id": "IE_US_RW_CoOxides",
          "name": "IE_US_RW",
          "description": "Trade from US to RW",
          "value": 0,
          "conceptReference": "IE_US_RW_CoOxides"
        }},
        {{
          "id": "IE_US_RW_CoUnwrought",
          "name": "IE_US_RW",
          "description": "Trade from US to RW",
          "value": 0,
          "conceptReference": "IE_US_RW_CoUnwrought"
        }},
        {{
          "id": "IE_CN_US_CoArticles",
          "name": "IE_CN_US",
          "description": "Trade from CN to US",
          "value": 0,
          "conceptReference": "IE_CN_US_CoArticles"
        }},
        {{
          "id": "IE_CN_US_CoOre",
          "name": "IE_CN_US",
          "description": "Trade from CN to US",
          "value": 0,
          "conceptReference": "IE_CN_US_CoOre"
        }},
        {{
          "id": "IE_CN_US_CoOxides",
          "name": "IE_CN_US",
          "description": "Trade from CN to US",
          "value": 0,
          "conceptReference": "IE_CN_US_CoOxides"
        }},
        {{
          "id": "IE_CN_US_CoUnwrought",
          "name": "IE_CN_US",
          "description": "Trade from CN to US",
          "value": 0,
          "conceptReference": "IE_CN_US_CoUnwrought"
        }},
        {{
          "id": "IE_US_CN_CoArticles",
          "name": "IE_US_CN",
          "description": "Trade from US to CN",
          "value": 0,
          "conceptReference": "IE_US_CN_CoArticles"
        }},
        {{
          "id": "IE_US_CN_CoOre",
          "name": "IE_US_CN",
          "description": "Trade from US to CN",
          "value": 0,
          "conceptReference": "IE_US_CN_CoOre"
        }},
        {{
          "id": "IE_US_CN_CoOxides",
          "name": "IE_US_CN",
          "description": "Trade from US to CN",
          "value": 0,
          "conceptReference": "IE_US_CN_CoOxides"
        }},
        {{
          "id": "IE_US_CN_CoUnwrought",
          "name": "IE_US_CN",
          "description": "Trade from US to CN",
          "value": 0,
          "conceptReference": "IE_US_CN_CoUnwrought"
        }},
        {{
          "id": "IE_CD_ZM_CoArticles",
          "name": "IE_CD_ZM",
          "description": "Trade from CD to ZM",
          "value": 0,
          "conceptReference": "IE_CD_ZM_CoArticles"
        }},
        {{
          "id": "IE_CD_ZM_CoOre",
          "name": "IE_CD_ZM",
          "description": "Trade from CD to ZM",
          "value": 0,
          "conceptReference": "IE_CD_ZM_CoOre"
        }},
        {{
          "id": "IE_CD_ZM_CoOxides",
          "name": "IE_CD_ZM",
          "description": "Trade from CD to ZM",
          "value": 0,
          "conceptReference": "IE_CD_ZM_CoOxides"
        }},
        {{
          "id": "IE_CD_ZM_CoUnwrought",
          "name": "IE_CD_ZM",
          "description": "Trade from CD to ZM",
          "value": 0,
          "conceptReference": "IE_CD_ZM_CoUnwrought"
        }},
        {{
          "id": "IE_ZM_CD_CoArticles",
          "name": "IE_ZM_CD",
          "description": "Trade from ZM to CD",
          "value": 0,
          "conceptReference": "IE_ZM_CD_CoArticles"
        }},
        {{
          "id": "IE_ZM_CD_CoOre",
          "name": "IE_ZM_CD",
          "description": "Trade from ZM to CD",
          "value": 0,
          "conceptReference": "IE_ZM_CD_CoOre"
        }},
        {{
          "id": "IE_ZM_CD_CoOxides",
          "name": "IE_ZM_CD",
          "description": "Trade from ZM to CD",
          "value": 0,
          "conceptReference": "IE_ZM_CD_CoOxides"
        }},
        {{
          "id": "IE_ZM_CD_CoUnwrought",
          "name": "IE_ZM_CD",
          "description": "Trade from ZM to CD",
          "value": 0,
          "conceptReference": "IE_ZM_CD_CoUnwrought"
        }},
        {{
          "id": "IE_CD_SA_CoArticles",
          "name": "IE_CD_SA",
          "description": "Trade from CD to SA",
          "value": 0,
          "conceptReference": "IE_CD_SA_CoArticles"
        }},
        {{
          "id": "IE_CD_SA_CoOre",
          "name": "IE_CD_SA",
          "description": "Trade from CD to SA",
          "value": 0,
          "conceptReference": "IE_CD_SA_CoOre"
        }},
        {{
          "id": "IE_CD_SA_CoOxides",
          "name": "IE_CD_SA",
          "description": "Trade from CD to SA",
          "value": 0,
          "conceptReference": "IE_CD_SA_CoOxides"
        }},
        {{
          "id": "IE_CD_SA_CoUnwrought",
          "name": "IE_CD_SA",
          "description": "Trade from CD to SA",
          "value": 0,
          "conceptReference": "IE_CD_SA_CoUnwrought"
        }},
        {{
          "id": "IE_SA_CD_CoArticles",
          "name": "IE_SA_CD",
          "description": "Trade from SA to CD",
          "value": 0,
          "conceptReference": "IE_SA_CD_CoArticles"
        }},
        {{
          "id": "IE_SA_CD_CoOre",
          "name": "IE_SA_CD",
          "description": "Trade from SA to CD",
          "value": 0,
          "conceptReference": "IE_SA_CD_CoOre"
        }},
        {{
          "id": "IE_SA_CD_CoOxides",
          "name": "IE_SA_CD",
          "description": "Trade from SA to CD",
          "value": 0,
          "conceptReference": "IE_SA_CD_CoOxides"
        }},
        {{
          "id": "IE_SA_CD_CoUnwrought",
          "name": "IE_SA_CD",
          "description": "Trade from SA to CD",
          "value": 0,
          "conceptReference": "IE_SA_CD_CoUnwrought"
        }},
        {{
          "id": "IE_CD_RW_CoArticles",
          "name": "IE_CD_RW",
          "description": "Trade from CD to RW",
          "value": 0,
          "conceptReference": "IE_CD_RW_CoArticles"
        }},
        {{
          "id": "IE_CD_RW_CoOre",
          "name": "IE_CD_RW",
          "description": "Trade from CD to RW",
          "value": 0,
          "conceptReference": "IE_CD_RW_CoOre"
        }},
        {{
          "id": "IE_CD_RW_CoOxides",
          "name": "IE_CD_RW",
          "description": "Trade from CD to RW",
          "value": 0,
          "conceptReference": "IE_CD_RW_CoOxides"
        }},
        {{
          "id": "IE_CD_RW_CoUnwrought",
          "name": "IE_CD_RW",
          "description": "Trade from CD to RW",
          "value": 0,
          "conceptReference": "IE_CD_RW_CoUnwrought"
        }},
        {{
          "id": "IE_RW_CD_CoArticles",
          "name": "IE_RW_CD",
          "description": "Trade from RW to CD",
          "value": 0,
          "conceptReference": "IE_RW_CD_CoArticles"
        }},
        {{
          "id": "IE_RW_CD_CoOre",
          "name": "IE_RW_CD",
          "description": "Trade from RW to CD",
          "value": 0,
          "conceptReference": "IE_RW_CD_CoOre"
        }},
        {{
          "id": "IE_RW_CD_CoOxides",
          "name": "IE_RW_CD",
          "description": "Trade from RW to CD",
          "value": 0,
          "conceptReference": "IE_RW_CD_CoOxides"
        }},
        {{
          "id": "IE_RW_CD_CoUnwrought",
          "name": "IE_RW_CD",
          "description": "Trade from RW to CD",
          "value": 0,
          "conceptReference": "IE_RW_CD_CoUnwrought"
        }},
        {{
          "id": "IE_CD_CN_CoArticles",
          "name": "IE_CD_CN",
          "description": "Trade from CD to CN",
          "value": 0,
          "conceptReference": "IE_CD_CN_CoArticles"
        }},
        {{
          "id": "IE_CD_CN_CoOre",
          "name": "IE_CD_CN",
          "description": "Trade from CD to CN",
          "value": 0,
          "conceptReference": "IE_CD_CN_CoOre"
        }},
        {{
          "id": "IE_CD_CN_CoOxides",
          "name": "IE_CD_CN",
          "description": "Trade from CD to CN",
          "value": 0,
          "conceptReference": "IE_CD_CN_CoOxides"
        }},
        {{
          "id": "IE_CD_CN_CoUnwrought",
          "name": "IE_CD_CN",
          "description": "Trade from CD to CN",
          "value": 0,
          "conceptReference": "IE_CD_CN_CoUnwrought"
        }},
        {{
          "id": "IE_CN_CD_CoArticles",
          "name": "IE_CN_CD",
          "description": "Trade from CN to CD",
          "value": 0,
          "conceptReference": "IE_CN_CD_CoArticles"
        }},
        {{
          "id": "IE_CN_CD_CoOre",
          "name": "IE_CN_CD",
          "description": "Trade from CN to CD",
          "value": 0,
          "conceptReference": "IE_CN_CD_CoOre"
        }},
        {{
          "id": "IE_CN_CD_CoOxides",
          "name": "IE_CN_CD",
          "description": "Trade from CN to CD",
          "value": 0,
          "conceptReference": "IE_CN_CD_CoOxides"
        }},
        {{
          "id": "IE_CN_CD_CoUnwrought",
          "name": "IE_CN_CD",
          "description": "Trade from CN to CD",
          "value": 0,
          "conceptReference": "IE_CN_CD_CoUnwrought"
        }},
        {{
          "id": "IE_SA_ZM_CoArticles",
          "name": "IE_SA_ZM",
          "description": "Trade from SA to ZM",
          "value": 0,
          "conceptReference": "IE_SA_ZM_CoArticles"
        }},
        {{
          "id": "IE_SA_ZM_CoOre",
          "name": "IE_SA_ZM",
          "description": "Trade from SA to ZM",
          "value": 0,
          "conceptReference": "IE_SA_ZM_CoOre"
        }},
        {{
          "id": "IE_SA_ZM_CoOxides",
          "name": "IE_SA_ZM",
          "description": "Trade from SA to ZM",
          "value": 0,
          "conceptReference": "IE_SA_ZM_CoOxides"
        }},
        {{
          "id": "IE_SA_ZM_CoUnwrought",
          "name": "IE_SA_ZM",
          "description": "Trade from SA to ZM",
          "value": 0,
          "conceptReference": "IE_SA_ZM_CoUnwrought"
        }},
        {{
          "id": "IE_ZM_SA_CoArticles",
          "name": "IE_ZM_SA",
          "description": "Trade from ZM to SA",
          "value": 0,
          "conceptReference": "IE_ZM_SA_CoArticles"
        }},
        {{
          "id": "IE_ZM_SA_CoOre",
          "name": "IE_ZM_SA",
          "description": "Trade from ZM to SA",
          "value": 0,
          "conceptReference": "IE_ZM_SA_CoOre"
        }},
        {{
          "id": "IE_ZM_SA_CoOxides",
          "name": "IE_ZM_SA",
          "description": "Trade from ZM to SA",
          "value": 0,
          "conceptReference": "IE_ZM_SA_CoOxides"
        }},
        {{
          "id": "IE_ZM_SA_CoUnwrought",
          "name": "IE_ZM_SA",
          "description": "Trade from ZM to SA",
          "value": 0,
          "conceptReference": "IE_ZM_SA_CoUnwrought"
        }},
        {{
          "id": "IE_RW_ZM_CoArticles",
          "name": "IE_RW_ZM",
          "description": "Trade from RW to ZM",
          "value": 0,
          "conceptReference": "IE_RW_ZM_CoArticles"
        }},
        {{
          "id": "IE_RW_ZM_CoOre",
          "name": "IE_RW_ZM",
          "description": "Trade from RW to ZM",
          "value": 0,
          "conceptReference": "IE_RW_ZM_CoOre"
        }},
        {{
          "id": "IE_RW_ZM_CoOxides",
          "name": "IE_RW_ZM",
          "description": "Trade from RW to ZM",
          "value": 0,
          "conceptReference": "IE_RW_ZM_CoOxides"
        }},
        {{
          "id": "IE_RW_ZM_CoUnwrought",
          "name": "IE_RW_ZM",
          "description": "Trade from RW to ZM",
          "value": 0,
          "conceptReference": "IE_RW_ZM_CoUnwrought"
        }},
        {{
          "id": "IE_ZM_RW_CoArticles",
          "name": "IE_ZM_RW",
          "description": "Trade from ZM to RW",
          "value": 0,
          "conceptReference": "IE_ZM_RW_CoArticles"
        }},
        {{
          "id": "IE_ZM_RW_CoOre",
          "name": "IE_ZM_RW",
          "description": "Trade from ZM to RW",
          "value": 0,
          "conceptReference": "IE_ZM_RW_CoOre"
        }},
        {{
          "id": "IE_ZM_RW_CoOxides",
          "name": "IE_ZM_RW",
          "description": "Trade from ZM to RW",
          "value": 0,
          "conceptReference": "IE_ZM_RW_CoOxides"
        }},
        {{
          "id": "IE_ZM_RW_CoUnwrought",
          "name": "IE_ZM_RW",
          "description": "Trade from ZM to RW",
          "value": 0,
          "conceptReference": "IE_ZM_RW_CoUnwrought"
        }},
        {{
          "id": "IE_CN_ZM_CoArticles",
          "name": "IE_CN_ZM",
          "description": "Trade from CN to ZM",
          "value": 0,
          "conceptReference": "IE_CN_ZM_CoArticles"
        }},
        {{
          "id": "IE_CN_ZM_CoOre",
          "name": "IE_CN_ZM",
          "description": "Trade from CN to ZM",
          "value": 0,
          "conceptReference": "IE_CN_ZM_CoOre"
        }},
        {{
          "id": "IE_CN_ZM_CoOxides",
          "name": "IE_CN_ZM",
          "description": "Trade from CN to ZM",
          "value": 0,
          "conceptReference": "IE_CN_ZM_CoOxides"
        }},
        {{
          "id": "IE_CN_ZM_CoUnwrought",
          "name": "IE_CN_ZM",
          "description": "Trade from CN to ZM",
          "value": 0,
          "conceptReference": "IE_CN_ZM_CoUnwrought"
        }},
        {{
          "id": "IE_ZM_CN_CoArticles",
          "name": "IE_ZM_CN",
          "description": "Trade from ZM to CN",
          "value": 0,
          "conceptReference": "IE_ZM_CN_CoArticles"
        }},
        {{
          "id": "IE_ZM_CN_CoOre",
          "name": "IE_ZM_CN",
          "description": "Trade from ZM to CN",
          "value": 0,
          "conceptReference": "IE_ZM_CN_CoOre"
        }},
        {{
          "id": "IE_ZM_CN_CoOxides",
          "name": "IE_ZM_CN",
          "description": "Trade from ZM to CN",
          "value": 0,
          "conceptReference": "IE_ZM_CN_CoOxides"
        }},
        {{
          "id": "IE_ZM_CN_CoUnwrought",
          "name": "IE_ZM_CN",
          "description": "Trade from ZM to CN",
          "value": 0,
          "conceptReference": "IE_ZM_CN_CoUnwrought"
        }},
        {{
          "id": "IE_RW_SA_CoArticles",
          "name": "IE_RW_SA",
          "description": "Trade from RW to SA",
          "value": 0,
          "conceptReference": "IE_RW_SA_CoArticles"
        }},
        {{
          "id": "IE_RW_SA_CoOre",
          "name": "IE_RW_SA",
          "description": "Trade from RW to SA",
          "value": 0,
          "conceptReference": "IE_RW_SA_CoOre"
        }},
        {{
          "id": "IE_RW_SA_CoOxides",
          "name": "IE_RW_SA",
          "description": "Trade from RW to SA",
          "value": 0,
          "conceptReference": "IE_RW_SA_CoOxides"
        }},
        {{
          "id": "IE_RW_SA_CoUnwrought",
          "name": "IE_RW_SA",
          "description": "Trade from RW to SA",
          "value": 0,
          "conceptReference": "IE_RW_SA_CoUnwrought"
        }},
        {{
          "id": "IE_SA_RW_CoArticles",
          "name": "IE_SA_RW",
          "description": "Trade from SA to RW",
          "value": 0,
          "conceptReference": "IE_SA_RW_CoArticles"
        }},
        {{
          "id": "IE_SA_RW_CoOre",
          "name": "IE_SA_RW",
          "description": "Trade from SA to RW",
          "value": 0,
          "conceptReference": "IE_SA_RW_CoOre"
        }},
        {{
          "id": "IE_SA_RW_CoOxides",
          "name": "IE_SA_RW",
          "description": "Trade from SA to RW",
          "value": 0,
          "conceptReference": "IE_SA_RW_CoOxides"
        }},
        {{
          "id": "IE_SA_RW_CoUnwrought",
          "name": "IE_SA_RW",
          "description": "Trade from SA to RW",
          "value": 0,
          "conceptReference": "IE_SA_RW_CoUnwrought"
        }},
        {{
          "id": "IE_CN_SA_CoArticles",
          "name": "IE_CN_SA",
          "description": "Trade from CN to SA",
          "value": 0,
          "conceptReference": "IE_CN_SA_CoArticles"
        }},
        {{
          "id": "IE_CN_SA_CoOre",
          "name": "IE_CN_SA",
          "description": "Trade from CN to SA",
          "value": 0,
          "conceptReference": "IE_CN_SA_CoOre"
        }},
        {{
          "id": "IE_CN_SA_CoOxides",
          "name": "IE_CN_SA",
          "description": "Trade from CN to SA",
          "value": 0,
          "conceptReference": "IE_CN_SA_CoOxides"
        }},
        {{
          "id": "IE_CN_SA_CoUnwrought",
          "name": "IE_CN_SA",
          "description": "Trade from CN to SA",
          "value": 0,
          "conceptReference": "IE_CN_SA_CoUnwrought"
        }},
        {{
          "id": "IE_SA_CN_CoArticles",
          "name": "IE_SA_CN",
          "description": "Trade from SA to CN",
          "value": 0,
          "conceptReference": "IE_SA_CN_CoArticles"
        }},
        {{
          "id": "IE_SA_CN_CoOre",
          "name": "IE_SA_CN",
          "description": "Trade from SA to CN",
          "value": 0,
          "conceptReference": "IE_SA_CN_CoOre"
        }},
        {{
          "id": "IE_SA_CN_CoOxides",
          "name": "IE_SA_CN",
          "description": "Trade from SA to CN",
          "value": 0,
          "conceptReference": "IE_SA_CN_CoOxides"
        }},
        {{
          "id": "IE_SA_CN_CoUnwrought",
          "name": "IE_SA_CN",
          "description": "Trade from SA to CN",
          "value": 0,
          "conceptReference": "IE_SA_CN_CoUnwrought"
        }},
        {{
          "id": "IE_CN_RW_CoArticles",
          "name": "IE_CN_RW",
          "description": "Trade from CN to RW",
          "value": 0,
          "conceptReference": "IE_CN_RW_CoArticles"
        }},
        {{
          "id": "IE_CN_RW_CoOre",
          "name": "IE_CN_RW",
          "description": "Trade from CN to RW",
          "value": 0,
          "conceptReference": "IE_CN_RW_CoOre"
        }},
        {{
          "id": "IE_CN_RW_CoOxides",
          "name": "IE_CN_RW",
          "description": "Trade from CN to RW",
          "value": 0,
          "conceptReference": "IE_CN_RW_CoOxides"
        }},
        {{
          "id": "IE_CN_RW_CoUnwrought",
          "name": "IE_CN_RW",
          "description": "Trade from CN to RW",
          "value": 0,
          "conceptReference": "IE_CN_RW_CoUnwrought"
        }},
        {{
          "id": "IE_RW_CN_CoArticles",
          "name": "IE_RW_CN",
          "description": "Trade from RW to CN",
          "value": 0,
          "conceptReference": "IE_RW_CN_CoArticles"
        }},
        {{
          "id": "IE_RW_CN_CoOre",
          "name": "IE_RW_CN",
          "description": "Trade from RW to CN",
          "value": 0,
          "conceptReference": "IE_RW_CN_CoOre"
        }},
        {{
          "id": "IE_RW_CN_CoOxides",
          "name": "IE_RW_CN",
          "description": "Trade from RW to CN",
          "value": 0,
          "conceptReference": "IE_RW_CN_CoOxides"
        }},
        {{
          "id": "IE_RW_CN_CoUnwrought",
          "name": "IE_RW_CN",
          "description": "Trade from RW to CN",
          "value": 0,
          "conceptReference": "IE_RW_CN_CoUnwrought"
        }},
        {{
          "id": "FWI_CD",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_CD"
        }},
        {{
          "id": "FWI_US",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_US"
        }},
        {{
          "id": "FWI_ZM",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_ZM"
        }},
        {{
          "id": "FWI_SA",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_SA"
        }},
        {{
          "id": "FWI_RW",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_RW"
        }},
        {{
          "id": "FWI_CN",
          "name": "FWI_CN(t)",
          "description": "Freedom House's Freedom in the World index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "FWI_CN"
        }},
        {{
          "id": "PPI_US",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_US"
        }},
        {{
          "id": "PPI_CD",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_CD"
        }},
        {{
          "id": "PPI_ZM",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_ZM"
        }},
        {{
          "id": "PPI_SA",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_SA"
        }},
        {{
          "id": "PPI_RW",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_RW"
        }},
        {{
          "id": "PPI_CN",
          "name": "PPI_CN(t)",
          "description": "Policy perception index of a country US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "PPI_CN"
        }},
        {{
          "id": "PP_CD_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CD_CoArticles"
        }},
        {{
          "id": "PP_CN_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CN_CoArticles"
        }},
        {{
          "id": "PP_RW_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_RW_CoArticles"
        }},
        {{
          "id": "PP_SA_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_SA_CoArticles"
        }},
        {{
          "id": "PP_US_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_US_CoArticles"
        }},
        {{
          "id": "PP_ZM_CoArticles",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_ZM_CoArticles"
        }},
        {{
          "id": "SP_CD_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CD_CoArticles"
        }},
        {{
          "id": "SP_CN_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CN_CoArticles"
        }},
        {{
          "id": "SP_RW_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_RW_CoArticles"
        }},
        {{
          "id": "SP_SA_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_SA_CoArticles"
        }},
        {{
          "id": "SP_US_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_US_CoArticles"
        }},
        {{
          "id": "SP_ZM_CoArticles",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_ZM_CoArticles"
        }},
        {{
          "id": "PP_US_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_US_CoOre"
        }},
        {{
          "id": "SP_US_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_US_CoOre"
        }},
        {{
          "id": "PP_US_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_US_CoOxides"
        }},
        {{
          "id": "SP_US_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_US_CoOxides"
        }},
        {{
          "id": "PP_US_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_US_CoUnwrought"
        }},
        {{
          "id": "SP_US_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_US_CoUnwrought"
        }},
        {{
          "id": "PP_CD_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CD_CoOre"
        }},
        {{
          "id": "SP_CD_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CD_CoOre"
        }},
        {{
          "id": "PP_CD_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CD_CoOxides"
        }},
        {{
          "id": "SP_CD_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CD_CoOxides"
        }},
        {{
          "id": "PP_CD_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CD_CoUnwrought"
        }},
        {{
          "id": "SP_CD_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CD_CoUnwrought"
        }},
        {{
          "id": "PP_ZM_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_ZM_CoOre"
        }},
        {{
          "id": "SP_ZM_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_ZM_CoOre"
        }},
        {{
          "id": "PP_ZM_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_ZM_CoOxides"
        }},
        {{
          "id": "SP_ZM_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_ZM_CoOxides"
        }},
        {{
          "id": "PP_ZM_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_ZM_CoUnwrought"
        }},
        {{
          "id": "SP_ZM_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_ZM_CoUnwrought"
        }},
        {{
          "id": "PP_SA_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_SA_CoOre"
        }},
        {{
          "id": "SP_SA_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_SA_CoOre"
        }},
        {{
          "id": "PP_SA_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_SA_CoOxides"
        }},
        {{
          "id": "SP_SA_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_SA_CoOxides"
        }},
        {{
          "id": "PP_SA_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_SA_CoUnwrought"
        }},
        {{
          "id": "SP_SA_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_SA_CoUnwrought"
        }},
        {{
          "id": "PP_RW_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_RW_CoOre"
        }},
        {{
          "id": "SP_RW_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_RW_CoOre"
        }},
        {{
          "id": "PP_RW_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_RW_CoOxides"
        }},
        {{
          "id": "SP_RW_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_RW_CoOxides"
        }},
        {{
          "id": "PP_RW_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_RW_CoUnwrought"
        }},
        {{
          "id": "SP_RW_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_RW_CoUnwrought"
        }},
        {{
          "id": "PP_CN_CoOre",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CN_CoOre"
        }},
        {{
          "id": "SP_CN_CoOre",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CN_CoOre"
        }},
        {{
          "id": "PP_CN_CoOxides",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CN_CoOxides"
        }},
        {{
          "id": "SP_CN_CoOxides",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CN_CoOxides"
        }},
        {{
          "id": "PP_CN_CoUnwrought",
          "name": "PP",
          "description": "Primary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "PP_CN_CoUnwrought"
        }},
        {{
          "id": "SP_CN_CoUnwrought",
          "name": "SP",
          "description": "Secondary production of a commodity in a country.",
          "value": 1,
          "conceptReference": "SP_CN_CoUnwrought"
        }},
        {{
          "id": "EXP_magneticalloys_CoOre",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_magneticalloys_CoOre"
        }},
        {{
          "id": "EXP_steels_CoOre",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_steels_CoOre"
        }},
        {{
          "id": "EXP_superalloys_CoOre",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_superalloys_CoOre"
        }},
        {{
          "id": "OP_magneticalloys",
          "name": "OP_magneticalloys(t)",
          "description": "Operating profit of an industry in the US.",
          "value": 1,
          "conceptReference": "OP_magneticalloys"
        }},
        {{
          "id": "OP_steels",
          "name": "OP_magneticalloys(t)",
          "description": "Operating profit of an industry in the US.",
          "value": 1,
          "conceptReference": "OP_steels"
        }},
        {{
          "id": "OP_superalloys",
          "name": "OP_magneticalloys(t)",
          "description": "Operating profit of an industry in the US.",
          "value": 1,
          "conceptReference": "OP_superalloys"
        }},
        {{
          "id": "VA_magneticalloys",
          "name": "VA_magneticalloys(t)",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US.",
          "value": 1,
          "conceptReference": "VA_magneticalloys"
        }},
        {{
          "id": "VA_steels",
          "name": "VA_magneticalloys(t)",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US.",
          "value": 1,
          "conceptReference": "VA_steels"
        }},
        {{
          "id": "VA_superalloys",
          "name": "VA_magneticalloys(t)",
          "description": "Value added (i.e. its contribution to a GDP) of an industry in the US.",
          "value": 1,
          "conceptReference": "VA_superalloys"
        }},
        {{
          "id": "EXP_magneticalloys_CoOxides",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_magneticalloys_CoOxides"
        }},
        {{
          "id": "EXP_steels_CoOxides",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_steels_CoOxides"
        }},
        {{
          "id": "EXP_superalloys_CoOxides",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_superalloys_CoOxides"
        }},
        {{
          "id": "EXP_magneticalloys_CoUnwrought",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_magneticalloys_CoUnwrought"
        }},
        {{
          "id": "EXP_steels_CoUnwrought",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_steels_CoUnwrought"
        }},
        {{
          "id": "EXP_superalloys_CoUnwrought",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_superalloys_CoUnwrought"
        }},
        {{
          "id": "EXP_magneticalloys_CoArticles",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_magneticalloys_CoArticles"
        }},
        {{
          "id": "EXP_steels_CoArticles",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_steels_CoArticles"
        }},
        {{
          "id": "EXP_superalloys_CoArticles",
          "name": "EXP_magneticalloys_CoArticles(t)",
          "description": "Expenditure of an industry on a commodity in the US.",
          "value": 1,
          "conceptReference": "EXP_superalloys_CoArticles"
        }},
        {{
          "id": "DeltaS_US_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_US_CoOre"
        }},
        {{
          "id": "DeltaS_US_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_US_CoOxides"
        }},
        {{
          "id": "DeltaS_US_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_US_CoUnwrought"
        }},
        {{
          "id": "DeltaS_US_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_US_CoArticles"
        }},
        {{
          "id": "DeltaS_CD_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CD_CoOre"
        }},
        {{
          "id": "DeltaS_CD_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CD_CoOxides"
        }},
        {{
          "id": "DeltaS_CD_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CD_CoUnwrought"
        }},
        {{
          "id": "DeltaS_CD_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CD_CoArticles"
        }},
        {{
          "id": "DeltaS_ZM_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_ZM_CoOre"
        }},
        {{
          "id": "DeltaS_ZM_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_ZM_CoOxides"
        }},
        {{
          "id": "DeltaS_ZM_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_ZM_CoUnwrought"
        }},
        {{
          "id": "DeltaS_ZM_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_ZM_CoArticles"
        }},
        {{
          "id": "DeltaS_SA_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_SA_CoOre"
        }},
        {{
          "id": "DeltaS_SA_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_SA_CoOxides"
        }},
        {{
          "id": "DeltaS_SA_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_SA_CoUnwrought"
        }},
        {{
          "id": "DeltaS_SA_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_SA_CoArticles"
        }},
        {{
          "id": "DeltaS_RW_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_RW_CoOre"
        }},
        {{
          "id": "DeltaS_RW_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_RW_CoOxides"
        }},
        {{
          "id": "DeltaS_RW_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_RW_CoUnwrought"
        }},
        {{
          "id": "DeltaS_RW_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_RW_CoArticles"
        }},
        {{
          "id": "DeltaS_CN_CoOre",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CN_CoOre"
        }},
        {{
          "id": "DeltaS_CN_CoOxides",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CN_CoOxides"
        }},
        {{
          "id": "DeltaS_CN_CoUnwrought",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CN_CoUnwrought"
        }},
        {{
          "id": "DeltaS_CN_CoArticles",
          "name": "DeltaS",
          "description": "Adjustments of industry and government stocks of a commodity in a country.",
          "value": 1,
          "conceptReference": "DeltaS_CN_CoArticles"
        }},
        {{
          "id": "g_US",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_US"
        }},
        {{
          "id": "g_CD",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_CD"
        }},
        {{
          "id": "g_ZM",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_ZM"
        }},
        {{
          "id": "g_SA",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_SA"
        }},
        {{
          "id": "g_RW",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_RW"
        }},
        {{
          "id": "g_CN",
          "name": "g",
          "description": "GDO growth rate of a country.",
          "value": 1,
          "conceptReference": "g_CN"
        }},
        {{
          "id": "MC_US",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_US"
        }},
        {{
          "id": "MC_CD",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_CD"
        }},
        {{
          "id": "MC_ZM",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_ZM"
        }},
        {{
          "id": "MC_SA",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_SA"
        }},
        {{
          "id": "MC_RW",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_RW"
        }},
        {{
          "id": "MC_CN",
          "name": "MC_CN(t)",
          "description": "Military cooperation, refers to whether the country has a current collective defense arrangement with the US US CD ZM SA RW CN.",
          "value": 1,
          "conceptReference": "MC_CN"
        }}
      ]

--- PARAMETERS END ---


Compose your reply with up to 4 top answers, with this JSON-format:

{{
  "answer": [
    {{ "name": <parameter_id>, "reason": <reason> }},
    {{ "name": <parameter_id>, "reason": <reason> }},
    ...
  ]
}}

The question is:

{question}

"""
