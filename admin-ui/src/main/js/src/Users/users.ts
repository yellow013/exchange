export type User = {
    userId: number
    username: string
    password: string
}

export type UserBalances = {
    [key: string]: number
}

export type GetUsersResponse = {
    users: User[]
}

export type GetUserBalancesResponse = {
    balances: UserBalances
}

export type AddUserRequest = {
    compId: string
}

export type ErrorResponse = {
    error?: string
}