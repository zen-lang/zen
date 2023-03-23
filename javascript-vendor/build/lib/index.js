"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.GetResources = exports.Client = void 0;
var axios_1 = require("axios");
var Client = /** @class */ (function () {
    function Client(baseURL, credentials) {
        this.client = axios_1.default.create({ baseURL: baseURL, auth: credentials });
    }
    Client.prototype.getResources = function (resourceName) {
        return new GetResources(this.client, resourceName);
    };
    Client.prototype.getResource = function (resourceName, id) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.get(resourceName + '/' + id)];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.findResources = function (resourceName, params) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.post(resourceName, { params: params })];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.deleteResource = function (resourceName, id) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.delete(resourceName + '/' + id)];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.createQuery = function (name, body) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.put("/AidboxQuery/".concat(name), body)];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.executeQuery = function (name, params) {
        return __awaiter(this, void 0, void 0, function () {
            var queryParams_1;
            return __generator(this, function (_a) {
                try {
                    queryParams_1 = new URLSearchParams();
                    if (params) {
                        Object.keys(params).map(function (key) {
                            var value = params[key];
                            if (value) {
                                queryParams_1.set(key, value.toString());
                            }
                        });
                    }
                    return [2 /*return*/, this.client.get("$query/".concat(name), {
                            params: queryParams_1,
                        })];
                }
                catch (e) {
                    throw e;
                }
                return [2 /*return*/];
            });
        });
    };
    Client.prototype.patchResource = function (resourceName, id, body) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.patch(resourceName + '/' + id, __assign({}, body))];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.createResource = function (resourceName, body) {
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.post(resourceName, __assign({}, body))];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.rawSQL = function (sql, params) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var body, response;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        body = __spreadArray([sql], ((_a = params === null || params === void 0 ? void 0 : params.map(function (value) { return value === null || value === void 0 ? void 0 : value.toString(); })) !== null && _a !== void 0 ? _a : []), true);
                        return [4 /*yield*/, this.client.post('/$sql', body)];
                    case 1:
                        response = _b.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.createSubscription = function (_a) {
        var id = _a.id, status = _a.status, trigger = _a.trigger, channel = _a.channel;
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.client.put("SubsSubscription/".concat(id), {
                            status: status,
                            trigger: trigger,
                            channel: __assign(__assign({}, channel), { type: 'rest-hook' }),
                        })];
                    case 1:
                        response = _b.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.bundleRequest = function (entry, type) {
        if (type === void 0) { type = 'transaction'; }
        return __awaiter(this, void 0, void 0, function () {
            var response;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.post("/", {
                            resourceType: 'Bundle',
                            type: type,
                            entry: entry,
                        })];
                    case 1:
                        response = _a.sent();
                        return [2 /*return*/, response.data];
                }
            });
        });
    };
    Client.prototype.bundleEntryPut = function (resource) {
        return {
            request: { method: 'PUT', url: "/".concat(resource.resourceType, "/").concat(resource.id) },
            resource: resource,
        };
    };
    Client.prototype.bundleEntryPost = function (resource) {
        return {
            request: { method: 'POST', url: "/".concat(resource.resourceType) },
            resource: resource,
        };
    };
    Client.prototype.bundleEntryPatch = function (resource) {
        return {
            request: { method: 'PATCH', url: "/".concat(resource.resourceType, "/").concat(resource.id) },
            resource: resource,
        };
    };
    Client.prototype.subscriptionEntry = function (_a) {
        var id = _a.id, status = _a.status, trigger = _a.trigger, channel = _a.channel;
        return {
            resourceType: 'SubsSubscription',
            id: id,
            status: status,
            trigger: trigger,
            channel: __assign(__assign({}, channel), { type: 'rest-hook' }),
        };
    };
    Client.prototype.sendLog = function (data) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.client.post('/$loggy', data)];
                    case 1:
                        _a.sent();
                        return [2 /*return*/];
                }
            });
        });
    };
    return Client;
}());
exports.Client = Client;
var GetResources = /** @class */ (function () {
    function GetResources(client, resourceName) {
        this.client = client;
        this.searchParamsObject = new URLSearchParams();
        this.resourceName = resourceName;
    }
    GetResources.prototype.where = function (key, value, prefix) {
        var _this = this;
        if (!Array.isArray(value)) {
            var queryValue = "".concat(prefix !== null && prefix !== void 0 ? prefix : '').concat(value);
            this.searchParamsObject.append(key.toString(), queryValue);
            return this;
        }
        if (prefix) {
            if (prefix === 'eq') {
                this.searchParamsObject.append(key.toString(), value.join(','));
                return this;
            }
            value.map(function (item) {
                _this.searchParamsObject.append(key.toString(), "".concat(prefix).concat(item));
            });
            return this;
        }
        var queryValues = value.join(',');
        this.searchParamsObject.append(key.toString(), queryValues);
        return this;
    };
    GetResources.prototype.contained = function (contained, containedType) {
        this.searchParamsObject.set('_contained', contained.toString());
        if (containedType) {
            this.searchParamsObject.set('_containedType', containedType);
        }
        return this;
    };
    GetResources.prototype.count = function (value) {
        this.searchParamsObject.set('_count', value.toString());
        return this;
    };
    GetResources.prototype.elements = function (args) {
        var queryValue = args.join(',');
        this.searchParamsObject.set('_elements', queryValue);
        return this;
    };
    GetResources.prototype.summary = function (type) {
        this.searchParamsObject.set('_summary', type.toString());
        return this;
    };
    GetResources.prototype.sort = function (key, dir) {
        var existedSortParams = this.searchParamsObject.get('_sort');
        if (existedSortParams) {
            var newSortParams = "".concat(existedSortParams, ",").concat(dir === 'asc' ? '-' : '').concat(key.toString());
            this.searchParamsObject.set('_sort', newSortParams);
            return this;
        }
        this.searchParamsObject.set('_sort', dir === 'asc' ? "-".concat(key.toString()) : key.toString());
        return this;
    };
    GetResources.prototype.then = function (onfulfilled, onrejected) {
        return this.client
            .get(this.resourceName, {
            params: this.searchParamsObject,
        })
            .then(function (response) {
            return onfulfilled ? onfulfilled(response.data) : response.data;
        });
    };
    return GetResources;
}());
exports.GetResources = GetResources;
