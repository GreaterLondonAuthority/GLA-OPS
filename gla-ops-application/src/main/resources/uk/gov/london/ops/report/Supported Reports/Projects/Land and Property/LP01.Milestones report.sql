select distinct
pj.project_id as "2 project_id",
pj.project_type  as "3 project_type",
pj.programme, 
max(case when mi.external_id = '3007' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "4 Strategy_agreed_Date",
max(case when mi.external_id = '3007' and mi.milestone_status is not null then mi.milestone_status else null end) as "5 Strategy_agreed_Status",
max(case when mi.external_id = '3014' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "6 MD_Date",
max(case when mi.external_id = '3014' and mi.milestone_status is not null then mi.milestone_status else null end) as "7 MD_Status",
max(case when mi.external_id = '3010' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "8 EOI_Date",
max(case when mi.external_id = '3010' and mi.milestone_status is not null then mi.milestone_status else null end) as "9 EOI_Status",
max(case when mi.external_id = '3011' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "10 FS_tender_recd_Date",
max(case when mi.external_id = '3011' and mi.milestone_status is not null then mi.milestone_status else null end) as "11 FS_tender_recd_Status",
max(case when mi.external_id = '3012' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "12 Final_tender_recd_Date",
max(case when mi.external_id = '3012' and mi.milestone_status is not null then mi.milestone_status else null end) as "13 Final_tender_recd_Status",
max(case when mi.external_id = '3013' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "14 Asst_comp_Date",
max(case when mi.external_id = '3013' and mi.milestone_status is not null then mi.milestone_status else null end) as "15 Asst_comp_Status",
max(case when mi.external_id = '3015' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "16 Standstill_comp_Date",
max(case when mi.external_id = '3015' and mi.milestone_status is not null then mi.milestone_status else null end) as "17 Standstill_comp_Status",
max(case when mi.external_id = '3016' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "18 Contract_exchd_Date",
max(case when mi.external_id = '3016' and mi.milestone_status is not null then mi.milestone_status else null end) as "19 Contract_exchd_Status",
max(case when mi.external_id = '3017' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "20 Contract_comp_Date",
max(case when mi.external_id = '3017' and mi.milestone_status is not null then mi.milestone_status else null end) as "21 Contract_comp_Status",
max(case when mi.external_id = '3031' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "22 Planning_granted_Date",
max(case when mi.external_id = '3031' and mi.milestone_status is not null then mi.milestone_status else null end) as "23 Planning_granted_Status",
max(case when mi.external_id in ('3003','3019') and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "24 SoS_Date",
max(case when mi.external_id in ('3003','3019') and mi.milestone_status is not null then mi.milestone_status else null end) as "25 SoS_Status",
max(case when mi.external_id = '3020' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "26 FoS_Date",
max(case when mi.external_id = '3020' and mi.milestone_status is not null then mi.milestone_status else null end) as "27 FoS_Status",
max(case when mi.external_id = '3021' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "28 Payment_recd_Date",
max(case when mi.external_id = '3021' and mi.milestone_status is not null then mi.milestone_status else null end) as "29 Payment_recd_Status",
max(case when mi.external_id = '3022' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "30 Stage1_budget_appd_Date",
max(case when mi.external_id = '3022' and mi.milestone_status is not null then mi.milestone_status else null end) as "31 Stage1_budget_appd_Status",
max(case when mi.external_id = '3023' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "32 Lawyers_instructed_Date",
max(case when mi.external_id = '3023' and mi.milestone_status is not null then mi.milestone_status else null end) as "33 Lawyers_instructed_Status",
max(case when mi.external_id = '3024' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "34 Agent_instructed_Date",
max(case when mi.external_id = '3024' and mi.milestone_status is not null then mi.milestone_status else null end) as "35 Agent_instructed_Status",
max(case when mi.external_id = '3025' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "36 Pre_contract_DD_Date",
max(case when mi.external_id = '3025' and mi.milestone_status is not null then mi.milestone_status else null end) as "37 Pre_contract_DD_Status",
max(case when mi.external_id = '3026' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "38 HoT_agreed_Date",
max(case when mi.external_id = '3026' and mi.milestone_status is not null then mi.milestone_status else null end) as "39 HoT_agreed_Status",
max(case when mi.external_id = '3027' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "40 Valuation_comp_Date",
max(case when mi.external_id = '3027' and mi.milestone_status is not null then mi.milestone_status else null end) as "41 Valuation_comp_Status",
max(case when mi.external_id = '3030' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "42 Payment_released_Date",
max(case when mi.external_id = '3030' and mi.milestone_status is not null then mi.milestone_status else null end ) as "43 Payment_released_Status",
max(case when mi.external_id = '3028' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end) as "44 Mkting_start_Date",
max(case when mi.external_id = '3028' and mi.milestone_status is not null then mi.milestone_status else null end) as "45 Mkting_start_Status",
max(case when mi.external_id = '3029' and mi.milestone_status is not null then to_char(milestone_date, 'dd/MM/yyyy') else null end ) as "46 Consideration_agreed_Date",
max(case when mi.external_id = '3029' and mi.milestone_status is not null then mi.milestone_status else null end ) as "47 Consideration_agreed_Status"


from project_block pb 
left join answer an on pb.id = an.questions_block
left join question q on an.question_id = q.id

inner join ( 	
  select * from (
select distinct pj.id, coalesce(pjdb.title,'') project_title, pj.org_id, coalesce(pj.status,'') status, pj.programme_id, coalesce(pg.name,'') programme
, coalesce(org.name,'') org_name, pjb.block_display_name, pjb.project_id, coalesce(pjdb.description,'')
description, w.name ward_name, pjdb.address, pjdb.borough, pjdb.postcode, t.name project_type
from project pj 
left join project_block pjb on pj.id = pjb.project_id
inner join project_details_block pjdb on pjb.id = pjdb.id
left join project_block_question pjbq on pjb.id = pjbq.project_block_id
left join organisation org on pj.org_id = org.id
left join programme pg on pj.programme_id = pg.id
left join ward w on pjdb.ward_id = w.id
left join template t on pj.template_id = t.id 
) a where a.programme_id in ( '1001','1004','1005') order by a.id
) pj on pb.project_id = pj.project_id

left join milestone mi on pb.id = mi.milestones_block

Group by 
pj.project_id,
pj.project_type,
pj.programme
