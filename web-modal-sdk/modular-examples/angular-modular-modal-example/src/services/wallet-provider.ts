import { IProvider } from "@web3auth/base";

import ethProvider from "./eth-provider";
import solanaProvider from "./solana-provider";

export interface IWalletProvider {
  getAccounts: () => Promise<any>;
  getBalance: () => Promise<any>;
  signAndSendTransaction: () => Promise<void>;
  signTransaction: () => Promise<void>;
  signMessage: () => Promise<void>;
}

export const getWalletProvider = (chain: string, provider: IProvider, uiConsole: any): IWalletProvider => {
  if (chain === "solana") {
    return solanaProvider(provider, uiConsole);
  }
  return ethProvider(provider, uiConsole);
};
