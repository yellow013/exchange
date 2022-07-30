export type Product = {
    productId: number
    symbol: string
    baseExponent: number
    counterExponent: number
    makerFee: number
    takerFee: number
}

export type GetProductsResponse = {
    products: Product[]
}