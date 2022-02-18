export interface User {
  loggedOn: boolean,
  SID?: string,
  isAdmin?: boolean,
  primaryRole?: string,
  permissions?: string[],
  username?:string,
  approved?:boolean,
  organisations?: { id: number; name: string; isManagingOrganisation: boolean }[]
}
