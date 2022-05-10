select distinct
org.id organisation_id,
org.name org_name,
org.managing_organisation_id,
pj.id Project_ID,
coalesce(pjdb.title,'') project_name,
coalesce(pj.status,'') status,
t.id template_id,
coalesce(t.name,'') project_type,
pj.programme_id,
coalesce(pg.name,'') programme,
coalesce(bo.borough,'') borough,
coalesce(route.route,'') as "Processing route",

case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end as "SoS approved date",

case when cast(substring((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end),4,2) as int) in ('1','2','3') then
text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end),4) as int)-1)||'-'||text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end),4) as int)) else
text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end),4) as int))||'-'||text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end),4) as int)+1) end
as "SoS approved financial year",


case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units1,0)+ coalesce(r2.units5,0) + coalesce(r1.units9,0)) else 0 end
as "Total units London Affordable Rent at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units2,0)+ coalesce(r2.units6,0) + coalesce(r1.units10,0)) else 0 end
as "Total units London Living Rent at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units3,0)+ coalesce(r2.units7,0) + coalesce(r1.units11,0)) else 0 end
as "Total units London Shared Ownership at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units4,0)+ coalesce(r2.units8,0) + coalesce(r1.units12,0)) else 0 end
as "Total units Other Affordable at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units41,0)+ coalesce(r2.units81,0) + coalesce(r1.units121,0)) else 0 end
as "Total Legacy Affordable Rent at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units42,0)+ coalesce(r2.units82,0) + coalesce(r1.units122,0)) else 0 end
as "Total Legacy Affordable Home Ownership at SoS",

coalesce(case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then q1.answer else null end,'')
as "Other Affordable unit type at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3.units1,0)+ coalesce(r2.units5,0) + coalesce(r1.units9,0) + coalesce(r3.units2,0)+ coalesce(r2.units6,0) + coalesce(r1.units10,0)
+coalesce(r3.units3,0)+ coalesce(r2.units7,0) + coalesce(r1.units11,0)+ coalesce(r3.units4,0)+ coalesce(r2.units8,0) + coalesce(r1.units12,0)
+coalesce(r3.units41,0)+ coalesce(r2.units81,0) + coalesce(r1.units121,0)+coalesce(r3.units42,0)+ coalesce(r2.units82,0) + coalesce(r1.units122,0)) else 0 end
as "Total Start on Site unit achieved",


case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then coalesce(s_grant.auth_amount,0) + coalesce(i_grant.auth_amount,0) else 0 end as "Grant paid at SoS",
case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then coalesce(s_rcgf.auth_amount,0) + coalesce(i_rcgf.auth_amount,0)  else 0 end as "RCGF Paid at SoS",
case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then coalesce(s_dpf.auth_amount,0) + coalesce(i_dpf.auth_amount,0) else 0 end as "DPF Paid at SoS",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_sos,'DD/MM/YYYY') else to_char(sos_date,'DD/MM/YYYY') end) is not null then coalesce(s_grant.auth_amount,0)+coalesce(s_rcgf.auth_amount,0)+coalesce(s_dpf.auth_amount,0) else 0 end
as "Total SOS GLA funding payment",

case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end as "Completion approved date",

case when cast(substring((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end),4,2) as int) in ('1','2','3') then
text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end),4) as int)-1)||'-'||text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end),4) as int)) else
text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end),4) as int))||'-'||text(cast(right((case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end),4) as int)+1) end
as "Comps approved financial year",


case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units1,0)+ coalesce(r2c.units5,0) + coalesce(r1c.units9,0)) else 0 end
as "Total units London Affordable Rent at Completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units2,0)+ coalesce(r2c.units6,0) + coalesce(r1c.units10,0)) else 0 end
as "Total units London Living Rent at Completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units3,0)+ coalesce(r2c.units7,0) + coalesce(r1c.units11,0)) else 0 end
as "Total units London Shared Ownership at Completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units4,0)+ coalesce(r2c.units8,0) + coalesce(r1c.units12,0)) else 0 end
as "Total units Other Affordable at Completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units41,0)+ coalesce(r2c.units81,0) + coalesce(r1c.units121,0)) else 0 end
as "Total Legacy Affordable Rent at Completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units42,0)+ coalesce(r2c.units82,0) + coalesce(r1c.units122,0)) else 0 end
as "Total Legacy Affordable Home Ownership at Completion",

coalesce(case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then q2.answer else null end,'')
as "Other Affordable unit type at completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then
(coalesce(r3c.units1,0)+ coalesce(r2c.units5,0) + coalesce(r1c.units9,0) + coalesce(r3c.units2,0)+ coalesce(r2c.units6,0) + coalesce(r1c.units10,0)
+coalesce(r3c.units3,0)+ coalesce(r2c.units7,0) + coalesce(r1c.units11,0)+ coalesce(r3c.units4,0)+ coalesce(r2c.units8,0) + coalesce(r1c.units12,0)
+coalesce(r3c.units41,0)+ coalesce(r2c.units81,0) + coalesce(r1c.units121,0)+coalesce(r3c.units42,0)+ coalesce(r2c.units82,0) + coalesce(r1c.units122,0)
) else 0 end
as "Total Completion units achieved",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then coalesce(c_grant.auth_amount,0) else 0 end as "Grant paid at completion",
case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then coalesce(c_rcgf.auth_amount,0) else 0 end as "RCGF paid at completion",
case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then coalesce(c_dpf.auth_amount,0) else 0 end as "DPF paid at completion",

case when (case when pj.programme_id in ('1010','1011','1012','1013','1014','1015') then to_char(lega_com,'DD/MM/YYYY') else to_char(com_date,'DD/MM/YYYY') end) is not null then coalesce(c_grant.auth_amount,0)+coalesce(c_rcgf.auth_amount,0)+coalesce(c_dpf.auth_amount,0) else 0 end
as "Total Comps GLA funding payment",


to_char(land_date,'DD/MM/YYYY') as "Land acquired approved date",
case when land_date is not null then coalesce(l_grant.auth_amount,0) else 0 end as "Grant paid at land acquired",
case when land_date is not null then coalesce(l_rcgf.auth_amount,0) else 0 end as "RCGF paid at land acquired",
case when land_date is not null then coalesce(l_dpf.auth_amount,0) else 0 end as "DPF paid at land acquired"


from project pj inner join project_block pb on pj.id = pb.project_id and pb.reporting_version ='true'

left join programme pg on pj.programme_id = pg.id
left join organisation org on pj.org_id = org.id
left join template t on pj.template_id = t.id
inner join project_details_block pjdb on pb.id = pjdb.id

left join (
select * from (
select distinct
pb.project_id ,
pjdb.borough
from project_block pb
inner join project_details_block pjdb on pb.id = pjdb.id
where pb.reporting_version ='true'
) a
)bo on pj.id = bo.project_id

left join (
select * from (
select distinct
pb.project_id ,
route.name route
from project_block pb
inner join  milestones_block mb on pb.id =mb.id
inner join processing_route route on mb.processing_route_id = route.id
where pb.reporting_version ='true'
) a
)route on pj.id = route.project_id


left join (
select * from (
select distinct
pb.project_id,
tb.sos_milestone_authorised lega_sos,
tb.completion_milestone_authorised lega_com

from tenure_block tb
inner join project_block pb on tb.id =pb.id
where pb.reporting_version ='true'
and (sos_milestone_authorised is not null or completion_milestone_authorised is not null )
) a
)lega on pj.id = lega.project_id





left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(cast(pb.last_modified as date)) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Start on site','Start on Site') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pj.id = sos.project_id

left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) com_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.external_id ='3004'and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)com on pj.id = com.project_id


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) modified
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Start on site','Start on Site') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id
where pb.block_display_name = 'Calculate Grant' and pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)sos_mod1 on pj.id = sos_mod1.project_id


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) modified
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) com_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.external_id ='3004' and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)com on pb.project_id = com.project_id
where pb.block_display_name = 'Calculate Grant' and pb.last_modified <= com.com_date
group by pb.project_id
) a
)com_mod1 on pj.id = com_mod1.project_id


left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod ,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_sos,0) else 0 end) units1,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_sos,0) else 0 end) units2,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_sos,0) else 0 end) units3,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_sos,0) else 0 end) units4,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_sos,0) else 0 end) units41,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_sos,0) else 0 end) units42,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost1,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost2,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost3,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost4,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost41,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost42


from project_block pb inner join project pj on pb.project_id = pj.id
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where  pb.block_display_name = 'Calculate Grant'
group by pb.project_id, pb.last_modified
  ) a
)r3 on pj.id = r3.project_id and sos_mod1.modified = r3.mod

left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod ,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_completion,0) else 0 end) units1,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_completion,0) else 0 end) units2,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_completion,0) else 0 end) units3,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_completion,0) else 0 end) units4,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_completion,0) else 0 end) units41,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_completion,0) else 0 end) units42,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost1,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost2,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost3,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost4,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost41,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost42

from project_block pb inner join project pj on pb.project_id = pj.id
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where  pb.block_display_name = 'Calculate Grant'
group by pb.project_id, pb.last_modified
  ) a
)r3c on pj.id = r3c.project_id and com_mod1.modified = r3c.mod


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Start on site','Start on Site') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id
where pb.block_display_name = 'Negotiated Grant' and pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)sos_mod2 on pj.id = sos_mod2.project_id


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.external_id ='3004'  and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id
where pb.block_display_name = 'Negotiated Grant' and pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)com_mod2 on pj.id = com_mod2.project_id


left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_sos,0)  else 0 end) units5,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_sos,0)  else 0 end) units6,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_sos,0)  else 0 end) units7,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_sos,0)  else 0 end) units8,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_sos,0)  else 0 end) units81,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_sos,0)  else 0 end) units82,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost5,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost6,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost7,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost8,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost81,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost82

from project_block pb inner join project pj on pb.project_id = pj.id and pj.total_grant_eligibility is not null
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where pb.block_display_name in ('Negotiated Grant')
group by pb.project_id, pb.last_modified
) a
)r2 on pj.id = r2.project_id and r2.mod = sos_mod2.mod


left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_completion,0)  else 0 end) units5,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_completion,0)  else 0 end) units6,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_completion,0)  else 0 end) units7,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_completion,0)  else 0 end) units8,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_completion,0)  else 0 end) units81,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_completion,0)  else 0 end) units82,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost5,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost6,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost7,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost8,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost81,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost82

from project_block pb inner join project pj on pb.project_id = pj.id and pj.total_grant_eligibility is not null
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where pb.block_display_name in ('Negotiated Grant')
group by pb.project_id, pb.last_modified
) a
)r2c on pj.id = r2c.project_id and r2c.mod = com_mod2.mod


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Start on site','Start on Site') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id
where pb.block_display_name = 'Developer-led Grant' and pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)sos_mod3 on pj.id = sos_mod3.project_id


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
inner join tenure_and_units ten on pb.id = ten.block_id
left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.external_id ='3004'  and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id
where pb.block_display_name = 'Developer-led Grant' and pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)com_mod3 on pj.id = com_mod3.project_id


left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_sos,0) else 0 end) units9,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_sos,0) else 0 end) units10,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_sos,0) else 0 end) units11,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_sos,0) else 0 end) units12,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_sos,0) else 0 end) units121,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_sos,0) else 0 end) units122,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost9,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost10,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost11,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost12,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost121,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost122

from project_block pb inner join project pj on pb.project_id = pj.id
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where pb.block_display_name = 'Developer-led Grant'
group by pb.project_id, pb.last_modified
) a
)r1 on pj.id = r1.project_id and sos_mod3.mod = r1.mod


left join (
  select * from (
select distinct
pb.project_id, pb.last_modified mod,
sum(case when ty.external_id ='4000' then coalesce(ten.total_units_at_completion,0) else 0 end) units9,
sum(case when ty.external_id ='4001' then coalesce(ten.total_units_at_completion,0) else 0 end) units10,
sum(case when ty.external_id ='4002' then coalesce(ten.total_units_at_completion,0) else 0 end) units11,
sum(case when ty.external_id ='4003' then coalesce(ten.total_units_at_completion,0) else 0 end) units12,
sum(case when ty.external_id ='4004' then coalesce(ten.total_units_at_completion,0) else 0 end) units121,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_units_at_completion,0) else 0 end) units122,


sum(case when ty.external_id ='4000' then coalesce(ten.total_cost,0) else 0 end) cost9,
sum(case when ty.external_id ='4001' then coalesce(ten.total_cost,0) else 0 end) cost10,
sum(case when ty.external_id ='4002' then coalesce(ten.total_cost,0) else 0 end) cost11,
sum(case when ty.external_id ='4003' then coalesce(ten.total_cost,0) else 0 end) cost12,
sum(case when ty.external_id ='4004' then coalesce(ten.total_cost,0) else 0 end) cost121,
sum(case when ty.external_id in ('4005','4006') then coalesce(ten.total_cost,0) else 0 end) cost122

from project_block pb inner join project pj on pb.project_id = pj.id
inner join tenure_and_units ten on pb.id = ten.block_id
inner join  template_tenure_type ty on ten.tenure_type_id = ty.id
where pb.block_display_name = 'Developer-led Grant'
group by pb.project_id, pb.last_modified
) a
)r1c on pj.id = r1c.project_id and com_mod3.mod = r1c.mod


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
left join answer an on pb.id = an.questions_block
inner join question q on an.question_id = q.id and q.id ='501'

inner join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Start on site','Start on Site') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id where pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)q_mod1 on pj.id = q_mod1.project_id

left join (
select * from (
select distinct
pj.id project_id, pb.last_modified mod,
an.question_id,
q.text,
an.answer
from project pj
left join project_block pb on pj.id = pb.project_id
left join answer an on pb.id = an.questions_block
inner join question q on an.question_id = q.id
and q.id ='501'
) a where a.answer is not null
) q1 on pj.id = q1.project_id and q1.mod = q_mod1.mod


left join (
select * from (
select distinct
pb.project_id, max(pb.last_modified) mod
from project_block pb
left join answer an on pb.id = an.questions_block
inner join question q on an.question_id = q.id and q.id ='501'
inner join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) sos_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Completion') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)sos on pb.project_id = sos.project_id where pb.last_modified <= sos.sos_date
group by pb.project_id
) a
)q_mod2 on pj.id = q_mod2.project_id

left join (
select * from (
select distinct
pj.id project_id, pb.last_modified mod,
an.question_id,
q.text,
an.answer
from project pj
left join project_block pb on pj.id = pb.project_id
left join answer an on pb.id = an.questions_block
inner join question q on an.question_id = q.id
and q.id ='501'
) a where a.answer is not null
) q2 on pj.id = q2.project_id and q2.mod = q_mod2.mod


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'PAYMENT'
and pl.sub_category in ('Start on site','Start on Site') and pl.authorised_on is not null
) a
) s_grant on pj.id = s_grant.project_id

left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'RCGF'
and pl.sub_category in ('Start on site','Start on Site') and pl.authorised_on is not null
) a
) s_rcgf on pj.id = s_rcgf.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'DPF'
and pl.sub_category in ('Start on site','Start on Site') and pl.authorised_on is not null
) a
) s_dpf on pj.id = s_dpf.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'PAYMENT'
and pl.sub_category ='Completion' and pl.authorised_on is not null
) a
) c_grant on pj.id = c_grant.project_id

left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'RCGF'
and pl.sub_category ='Completion' and pl.authorised_on is not null
) a
) c_rcgf on pj.id = c_rcgf.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where pl.ledger_type = 'DPF'
and pl.sub_category ='Completion' and pl.authorised_on is not null
) a
) c_dpf on pj.id = c_dpf.project_id



left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) land_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Land acquired') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)land on pj.id = land.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'PAYMENT'
and pl.sub_category ='Land acquired' and pl.authorised_on is not null
) a
) l_grant on pj.id = l_grant.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'RCGF'
and pl.sub_category ='Land acquired' and pl.authorised_on is not null
) a
) l_rcgf on pj.id = l_rcgf.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'DPF'
and pl.sub_category ='Land acquired' and pl.authorised_on is not null
) a
) l_dpf on pj.id = l_dpf.project_id




left join (
select * from (
select distinct
pb.project_id,
mi.claim_status,
min(pb.last_modified) interim_date
from project_block pb
inner join milestone mi on pb.id = mi.milestones_block
where mi.summary in ('Interim payment') and mi.claim_status = 'Approved'
and pb.block_status in ('APPROVED','LAST_APPROVED')
group by pb.project_id,  mi.claim_status
) a
)interim on pj.id = interim.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'PAYMENT'
and pl.sub_category ='Interim payment' and pl.authorised_on is not null
) a
) i_grant on pj.id = i_grant.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where  pl.ledger_type = 'RCGF'
and pl.sub_category ='Interim payment' and pl.authorised_on is not null
) a
) i_rcgf on pj.id = i_rcgf.project_id


left join (
select * from (
select distinct
pl.project_id ,
pl.amount*-1 auth_amount,
to_char(pl.authorised_on,'DD/MM/YYYY') auth_date

from project_ledger_entry pl
inner join project pj on pl.project_id = pj.id
inner join project_block pb on pj.id = pb.project_id

where pl.ledger_type = 'DPF'
and pl.sub_category ='Interim payment' and pl.authorised_on is not null
) a
) i_dpf on pj.id = i_dpf.project_id
and pj.programme_id in (:programme_ids)



order by 3