import axios from "axios"
import { adminApiUrl } from "../constants"
import { GetAssetsResponse } from "./assets"

const api = axios.create({
    baseURL: adminApiUrl,
})

export const getAssets: () => Promise<GetAssetsResponse> = () => api.get("/assets").then((response) => response.data)

export const addAsset: (symbol: string, scale: string) => Promise<number> = (symbol, scale) =>
    api.post("/asset", { symbol, scale }).then(response => response.status)
