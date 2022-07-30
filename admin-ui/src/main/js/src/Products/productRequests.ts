import axios from "axios"
import { adminApiUrl } from "../constants"
import { GetProductsResponse } from "./products"

const api = axios.create({
    baseURL: adminApiUrl,
})

export const getProducts: () => Promise<GetProductsResponse> = () => api.get("/products").then((response) => response.data)

export const addProduct: (baseAssetId: number, counterAssetId: number, makerFee: number, takerFee: number) => Promise<number> = (baseAssetId, counterAssetId, makerFee, takerFee) =>
    api.post("/product", { baseAssetId, counterAssetId, makerFee, takerFee }).then(response => response.status)
