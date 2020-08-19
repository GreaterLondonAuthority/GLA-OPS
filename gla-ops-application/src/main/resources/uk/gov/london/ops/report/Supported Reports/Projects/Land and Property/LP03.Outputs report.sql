select distinct 
pj.project_id as "48 Project_ID",
coalesce(pj.programme,'') programme,
coalesce(occ.category,'') as "49 Output_Category",
coalesce(occ.subcategory,'') as "50 Sub_Category",
coalesce(ote.output_type,'') as "51 Output_Type", 
coalesce(occ.value_type,'') as "52 Output_Value",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month < '201204' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month < '201204' then ote.actual else 0 end)/7,0),0) end as "53 Sum_Pre2012_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201204'and'201303' then ote.actual else 0 end)/7,0),1) 
else round(coalesce(sum(case when ote.year_month between '201204'and'201303' then ote.actual else 0 end)/7,0),0) end as "54 2012_13_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201304'and'201403' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201304'and'201403' then ote.actual else 0 end)/7,0),0) end as "55 2013_14_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201404'and'201503' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201404'and'201503' then ote.actual else 0 end)/7,0),0) end as "56 2014_15_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201504'and'201603' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201504'and'201603' then ote.actual else 0 end)/7,0),0) end as "57 2015_16_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201604'and'201703' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201604'and'201703' then ote.actual else 0 end)/7,0),0) end as "58 2016_17_Total_Actual",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201704' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201704' then ote.forecast else 0 end)/7,0),0) end as "59 2017_18_Fcast_Apr",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201704' then ote.actual else 0 end)/7,0),1) 
else round(coalesce(sum(case when ote.year_month = '201704' then ote.actual else 0 end)/7,0),0) end as "60 2017_18_Actual_Apr",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201705' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201705' then ote.forecast else 0 end)/7,0),0) end as "61 2017_18_Fcast_May",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201705' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201705' then ote.actual else 0 end)/7,0),0) end as "62 2017_18_Actual_May",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201706' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201706' then ote.forecast else 0 end)/7,0),0) end as "63 2017_18_Fcast_Jun",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201706' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201706' then ote.actual else 0 end)/7,0),0) end as "64 2017_18_Actual_Jun",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201707' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201707' then ote.forecast else 0 end)/7,0),0) end as "65 2017_18_Fcast_Jul",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201707' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201707' then ote.actual else 0 end)/7,0),0) end as "66 2017_18_Actual_Jul",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201708' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201708' then ote.forecast else 0 end)/7,0),0) end as "67 2017_18_Fcast_Aug",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201708' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201708' then ote.actual else 0 end)/7,0),0) end as "68 2017_18_Actual_Aug",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201709' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201709' then ote.forecast else 0 end)/7,0),0) end as "69 2017_18_Fcast_Sep",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201709' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201709' then ote.actual else 0 end)/7,0),0) end as "70 2017_18_Actual_Sep",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201710' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201710' then ote.forecast else 0 end)/7,0),0) end as "71 2017_18_Fcast_Oct",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201710' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201710' then ote.actual else 0 end)/7,0),0) end as "72 2017_18_Actual_Oct",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201711' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201711' then ote.forecast else 0 end)/7,0),0) end as "73 2017_18_Fcast_Nov",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201711' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201711' then ote.actual else 0 end)/7,0),0) end as "74 2017_18_Actual_Nov",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201712' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201712' then ote.forecast else 0 end)/7,0),0) end as "75 2017_18_Fcast_Dec",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201712' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201712' then ote.actual else 0 end)/7,0),0) end as "76 2017_18_Actual_Dec",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201801' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201801' then ote.forecast else 0 end)/7,0),0) end as "77 2017_18_Fcast_Jan",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201801' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201801' then ote.actual else 0 end)/7,0),0) end as "78 2017_18_Actual_Jan",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201802' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201802' then ote.forecast else 0 end)/7,0),0) end as "79 2017_18_Fcast_Feb",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201802' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201802' then ote.actual else 0 end)/7,0),0) end as "80 2017_18_Actual_Feb",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201803' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201803' then ote.forecast else 0 end)/7,0),0) end as "81 2017_18_Fcast_Mar",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month = '201803' then ote.actual else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month = '201803' then ote.actual else 0 end)/7,0),0) end as "82 2017_18_Actual_Mar",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201804'and'201903' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201804'and'201903' then ote.forecast else 0 end)/7,0),0) end as "83 2018_19_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '201904'and'202003' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '201904'and'202003' then ote.forecast else 0 end)/7,0),0) end as "84 2019_20_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '202004'and'202103' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '202004'and'202103' then ote.forecast else 0 end)/7,0),0) end as "85 2020_21_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '202104'and'202203' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '202104'and'202203' then ote.forecast else 0 end)/7,0),0) end as "86 2021_22_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '202204'and'202303' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '202204'and'202303' then ote.forecast else 0 end)/7,0),0) end as "87 2022_23_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '202304'and'202403' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '202304'and'202403' then ote.forecast else 0 end)/7,0),0) end as "88 2023_24_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month between '202404'and'202503' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month between '202404'and'202503' then ote.forecast else 0 end)/7,0),0) end as "89 2024_25_Total_Fcast",
case when occ.category='Land Brought into Beneficial Use (Ha)' then round(coalesce(sum(case when ote.year_month > '202503' then ote.forecast else 0 end)/7,0),1)
else round(coalesce(sum(case when ote.year_month > '202503' then ote.forecast else 0 end)/7,0),0) end as "90 2025+_Total_Fcast"
from project_block pb 
inner join ( 
select * from (
select distinct 
pj.id project_id, 
pj.programme_id, 
coalesce(pg.name,'') programme, 
t.name project_type,
cast(EXTRACT(YEAR FROM current_date) as int) ||''|| cast (EXTRACT(MONTH FROM current_date) as int) as "current"
from project pj 
left join project_block pjb on pj.id = pjb.project_id
left join organisation org on pj.org_id = org.id
left join programme pg on pj.programme_id = pg.id
left join template t on pj.template_id = t.id 
where programme_id in ( '1001','1004','1005')
) a order by a.project_id
) pj on pb.project_id = pj.project_id
left join output_table_entry ote on pb.project_id = ote.project_id 
left join output_cat_config occ on ote.configuration_id = occ.id
group by 
pj.project_id,
pj.programme,
occ.category,
occ.subcategory,
ote.output_type, 
occ.value_type
order by 1, 3, 4, 5