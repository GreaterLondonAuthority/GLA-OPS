select distinct
org.id organisation_id,
org.managing_organisation_id,
pl.project_id,
pj.programme_id,
pg.name programme_name,
t.id template_id,
t.name template_name,
borough,
title.pcs_project_code pcs_project_number,
title.title project_name,

case when month in ('1','2','3') then text(cast(year as int)-1) ||'/'|| text(cast(year as int))
else text(cast(year as int)) ||'/'|| text(cast(year as int)+1)  end financial_year,

case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='4'
or (EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='5' and
EXTRACT(DAY FROM  to_date(transaction_date,'DD/MM/YYYY')) in ('1','2') )
then '1' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='5' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '3' and '30' then '2' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='6' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '27'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='5' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) ='31')
then '3' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='7' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '25'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='6' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '28' and '30')
then '4' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='7' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '26' and '31'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='8' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '22')
then '5' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='8' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '23' and '31'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='9' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '19')
then '6' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='9' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '20' and '30'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='10' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '17')
then '7' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='10' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '18' and '31'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='11' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '14')
then '8' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='11' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '15' and '30'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='12' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '12')
then '9' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='12' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '13' and '31'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='1' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '9')
then '10' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='1' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '10' and '31'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='2' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '6')
then '11' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='2' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '7' and '30'
or ( EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='3' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '1' and '5')
then '12' else

(case when EXTRACT(MONTH FROM to_date(transaction_date,'DD/MM/YYYY')) ='3' and
EXTRACT(DAY FROM to_date(transaction_date,'DD/MM/YYYY')) between '6' and '31'
then '13' else null end
)

end)end)end)end)end)end)end)end)end)end)end)end sap_period,


year,
month,
ledger_status,
ledger_type,
spend_type,
category,
amount,
pl.interest,
reference,
pcs_phase_number,
vendor_name,
organisation_id,
transaction_date,
sap_category_code,
pl.description,
cost_centre_code,
transaction_number,
to_char(pl.created_on,'dd/MM/yyyy') created_on,
pl.created_by,
pl.modified_by,
pl.invoice_date,
ledger_source,
pl.wbs_code,
to_char(pl.authorised_on,'dd/MM/yyyy') authorised_on,
authorised_by,
to_char(pl.sent_on,'dd/MM/yyyy') sent_on,
to_char(pl.acknowledged_on,'dd/MM/yyyy')acknowledged_on,
to_char(pl.cleared_on,'dd/MM/yyyy') cleared_on,
sub_category,
invoice_filename,
pl.sap_vendor_id,
external_id




from project_ledger_entry pl
inner join project_block pb ON pl.project_id = pb.project_id and pl.block_id = pb.id
and pb.reporting_version ='true'
inner join project pj ON pb.project_id = pj.id
left join template t on pj.template_id = t.id
inner join programme pg on pj.programme_id = pg.id
left join organisation org on pj.org_id = org.id

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
pb.project_id,  pjdb.title, pcs_project_code
from project_block pb
inner join project_details_block pjdb on pb.id = pjdb.id
where block_display_name = 'Project Details'
and reporting_version = 'true'
) a
)title on title.project_id = pl.project_id

and pj.programme_id in (:programme_ids)

