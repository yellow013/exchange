import axios from "axios";
import { Balances, UserCredentials } from "types";
import { baseURL } from "./constants";


export const api = axios.create({
    baseURL: baseURL,
})


export const getBalances: (credentials: Partial<UserCredentials>) => Promise<Balances> = (credentials) => {
    return api.get("/balances", { auth: makeCredentials(credentials) }).then(response => response.data)
}

export const getProducts: (credentials: Partial<UserCredentials>) => Promise<string[]> = (credentials) => {
    return api.get("/products", { auth: makeCredentials(credentials) }).then(response => response.data.products)
}

export const placeOrder: (credentials: Partial<UserCredentials>, product: string, side: string, price: string, amount: string) => Promise<number> = (credentials, product, side, price, amount) => {
    return api.post("/placeOrder", { "product": product, "side": side, "price": price, "amount": amount }, { auth: makeCredentials(credentials) })
    .then(response => response.status)
}

const makeCredentials = (credentials: Partial<UserCredentials>) => ({ username: String(credentials.username), password: String(credentials.password) })