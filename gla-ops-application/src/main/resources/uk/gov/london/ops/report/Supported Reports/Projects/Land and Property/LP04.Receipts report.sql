select distinct 
pj.id project_id, 
coalesce(pg.name,'') programme,
abs(round(coalesce(sum(case when pl.year_month ='201801'and pl.ledger_status = 'ACTUAL' then pl.amount else 0 end),0),0)) as "191 Jan_Curr_yr_Act",
abs(round(coalesce(sum(case when pl.year_month ='201802'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "192 Feb_Curr_yr_Fcast",
abs(round(coalesce(sum(case when pl.year_month ='201802'and pl.ledger_status = 'ACTUAL' then pl.amount else 0 end),0),0)) as "193 Feb_Curr_yr_Act",
abs(round(coalesce(sum(case when pl.year_month ='201803'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "194 Mar_Curr_yr_Fcast",
abs(round(coalesce(sum(case when pl.year_month ='201803'and pl.ledger_status = 'ACTUAL' then pl.amount else 0 end),0),0)) as "195 Mar_Curr_yr_Act",
abs(round(coalesce(sum(case when pl.year_month between'201804' and '201903'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "196 2018_19_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'201904' and '202003'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "197 2019_20_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202004' and '202103'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "198 2020_21_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202104' and '202203'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "199 2021_22_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202204' and '202303'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "200 2022_23_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202304' and '202403'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "201 2023_24_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202404' and '202503'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "202 2024_25_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202504' and '202603'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "203 2025_26_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202604' and '202703'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "204 2026_27_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202704' and '202803'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "205 2027_28_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202804' and '202903'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "206 2028_29_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month between'202904' and '203003'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "207 2029_30_Tot_Fcast",
abs(round(coalesce(sum(case when pl.year_month >'203004'and pl.ledger_status = 'FORECAST' then pl.amount else 0 end),0),0)) as "208 Sum_2030+_Tot_Fcast"
from project pj 
left join project_block pjb on pj.id = pjb.project_id and block_display_name ='Receipts'
left join organisation org on pj.org_id = org.id
left join programme pg on pj.programme_id = pg.id
left join project_ledger_entry pl on pjb.project_id = pl.project_id and pjb.id = pl.block_id and pl.amount is not null and pl.amount <>0
where programme_id in ( '1001','1004','1005') 
group by 
pj.id ,
pg.name