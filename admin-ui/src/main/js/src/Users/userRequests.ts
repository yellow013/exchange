import axios from "axios"
import { GetUserBalancesResponse, GetUsersResponse } from "Users/users"
import { adminApiUrl } from "../constants"

const api = axios.create({
    baseURL: adminApiUrl,
})

export const getUsers: () => Promise<GetUsersResponse> = () => api.get("/users").then((response) => response.data)

export const addUser: (username: string, password: string) => Promise<number> = (username, password) =>
    api.post("/user", { username, password }).then(response => response.status)


export const getUserBalances: (userId: number) => Promise<GetUserBalancesResponse> = (userId) => api.get(`/${userId}/balances`).then((response) => response.data)

export const updateUserBalance: (userId: number, asset: string, amount: number) => Promise<number> = (userId, asset, amount) =>
    api.put(`/${userId}/${asset}/balance`, { amount }).then(response => response.status)